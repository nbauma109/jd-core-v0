package jd.core.instruction.bytecode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.Method;
import jd.core.classfile.attribute.CodeException;
import jd.core.classfile.attribute.LineNumber;
import jd.core.instruction.bytecode.factory.InstructionFactory;
import jd.core.instruction.bytecode.factory.InstructionFactoryConstants;
import jd.core.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.ReturnAddressLoad;
import jd.core.instruction.bytecode.util.ByteCodeUtil;
import jd.core.util.IntSet;


public class InstructionListBuilder 
{
	private static CodeExceptionComparator COMPARATOR = 
		new CodeExceptionComparator();
	
	public static void Build(
			ClassFile classFile, Method method, 
			List<Instruction> list, 
			List<Instruction> listForAnalyze)
		throws Exception
	{
		byte[] code = method.getCode();
		
		if (code != null)
		{
			try
			{
				final int length = code.length;
				
				// Declaration du tableau de sauts utile pour reconstruire les 
				// instructions de pre et post incrementation : si une 
				// instruction 'iinc' est une instruction vers laquelle on 
				// saute, elle ne sera pas agreg�e a l'instruction precedante 
				// ou suivante.			
				boolean[] jumps = new boolean[length];
				
				// Declaration du tableau des sauts vers les sous procedures 
				// (jsr ... ret). A chaque d�but de sous procedures, une pseudo 
				// adresse de retour doit etre inseree sur la pile.
				IntSet offsetSet = new IntSet();
				
				// Population des deux tableaux dans la meme passe.
				PopulateJumpsArrayAndSubProcOffsets(code, length, jumps, offsetSet);	
							
				// Initialisation de variables additionnelles pour le traitement 
				// des sous procedures.
				int[] subProcOffsets = offsetSet.toArray();
				int subProcOffsetsIndex = 0;
				int subProcOffset = 
					(subProcOffsets == null) ? -1 : subProcOffsets[0];
				
				// Declaration de variables additionnelles pour le traitement 
				// des blocs 'catch' et 'finally'.
				final Stack<Instruction> stack = new Stack<Instruction>();		
				final CodeException[] codeExceptions = method.getCodeExceptions();
				int codeExceptionsIndex = 0;
				int exceptionOffset;
				ConstantPool constants = classFile.getConstantPool();
							
				if (codeExceptions == null)
				{
					exceptionOffset = -1;
				}
				else
				{
					// Sort codeExceptions by handler_pc
					Arrays.sort(codeExceptions, COMPARATOR);
					exceptionOffset = codeExceptions[0].handler_pc;
				}
				
	            // Declaration de variables additionnelles pour le traitement 
				// des numeros de ligne
				LineNumber[] lineNumbers = method.getLineNumbers();
				int lineNumbersIndex = 0;				
				int lineNumber;
				int nextLineOffset;
				
				if (lineNumbers == null)
				{
					lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
					nextLineOffset = -1;
				}
				else
				{
					LineNumber ln = lineNumbers[lineNumbersIndex];
					lineNumber = ln.line_number;
					nextLineOffset = -1;
					
					int startPc = ln.start_pc;					
					while (++lineNumbersIndex < lineNumbers.length)
					{
						ln = lineNumbers[lineNumbersIndex];
						if (ln.start_pc != startPc)
						{
							nextLineOffset = ln.start_pc;
							break;
						}
						lineNumber = ln.line_number;
					}
				}
				
				// Boucle principale : agregation des instructions
				for (int offset=0; offset<length; ++offset)
				{
					int opcode = code[offset] & 255;
					InstructionFactory factory = 
						InstructionFactoryConstants.FACTORIES[opcode];
					
					if (factory != null)
					{
						// Ajout de ExceptionLoad
						if (offset == exceptionOffset)
						{
							// Ajout d'une pseudo instruction de lecture 
							// d'exception en debut de bloc catch
							int catchType = 
								codeExceptions[codeExceptionsIndex].catch_type;
							int signatureIndex = (catchType==0) ? 0 : 
								constants.addConstantUtf8(
									'L' + constants.getConstantClassName(catchType) + ';');					
	
							ExceptionLoad el = new ExceptionLoad(
									ByteCodeConstants.EXCEPTIONLOAD, 
									offset, lineNumber, signatureIndex);						
							stack.push(el);						
							listForAnalyze.add(el);
							
							// Search next exception offset
							int nextOffsetException;
							for (;;)
							{
								if (++codeExceptionsIndex >= codeExceptions.length)
								{
									nextOffsetException = -1;
									break;
								}
								
								nextOffsetException = 
									codeExceptions[codeExceptionsIndex].handler_pc;
								
								if (nextOffsetException != exceptionOffset)
									break;
							}						
							exceptionOffset = nextOffsetException;
						}
						
						// Ajout de ReturnAddressLoad
						if (offset == subProcOffset)
						{
							// Ajout d'une pseudo adresse de retour en debut de 
							// sous procedure. Lors de l'execution, cette 
							// adresse est normalement plac�e sur la pile par 
							// l'instruction JSR.
							stack.push(new ReturnAddressLoad(
									ByteCodeConstants.RETURNADDRESSLOAD, 
									offset, lineNumber));
									
							if (++subProcOffsetsIndex >= subProcOffsets.length)
								subProcOffset = -1;
							else
								subProcOffset = subProcOffsets[subProcOffsetsIndex];
						}
						
						// Traitement des numeros de ligne
						if (offset == nextLineOffset)
						{
							LineNumber ln = lineNumbers[lineNumbersIndex];
							lineNumber = ln.line_number;
							nextLineOffset = -1;
							
							int startPc = ln.start_pc;					
							while (++lineNumbersIndex < lineNumbers.length)
							{
								ln = lineNumbers[lineNumbersIndex];
								if (ln.start_pc != startPc)
								{
									nextLineOffset = ln.start_pc;
									break;
								}
								lineNumber = ln.line_number;
							}
						}
						
						// Generation d'instruction
						offset += factory.create(
							classFile, method, list, listForAnalyze, stack, 
							code, offset, lineNumber, jumps);
					}
					else
					{
						System.err.println(
								"No factory for " + 
								ByteCodeConstants.OPCODE_NAMES[opcode]);
						System.exit(0);
					}
				}
			
				if (! stack.isEmpty())
				{
					final String className = classFile.getClassName();
					final String methodName =
						classFile.getConstantPool().getConstantUtf8(method.name_index);
					System.err.println(
						"'" + className + '.' + methodName + 
						"' build error: stack not empty. stack=" + stack);
				}
			}
			catch (Exception e)
			{
				// Bad byte code ... generate, for example, by Eclipse Java 
				// Compiler or Harmony:
			    // Byte code:
			    //   0: aload_0
			    //   1: invokevirtual 16	TryCatchFinallyClassForTest:before	()V
			    //   4: iconst_1
			    //   5: ireturn
			    //   6: astore_1       <----- Error: EmptyStackException
			    //   7: aload_0
			    //   8: invokevirtual 19	TryCatchFinallyClassForTest:inCatch1	()V
			    //   11: aload_0
			    //   12: invokevirtual 22	TryCatchFinallyClassForTest:after	()V
			    //   15: iconst_2
			    //   16: ireturn				
				throw e;
			}
		}
	}
	
	private static void PopulateJumpsArrayAndSubProcOffsets(
			byte[] code, int length, boolean[] jumps, IntSet offsetSet)
	{
		for (int offset=0; offset<length; ++offset)
		{
			int jumpOffset;
			int opcode = code[offset] & 255;
			
			switch (ByteCodeConstants.NO_OF_OPERANDS[opcode])
			{
			case 0:
				break;
			case 2:
				switch (opcode)
				{
				case ByteCodeConstants.IFEQ: 
				case ByteCodeConstants.IFNE:
				case ByteCodeConstants.IFLT: 
				case ByteCodeConstants.IFGE:
				case ByteCodeConstants.IFGT: 
				case ByteCodeConstants.IFLE:

				case ByteCodeConstants.IF_ICMPEQ: 
				case ByteCodeConstants.IF_ICMPNE:
				case ByteCodeConstants.IF_ICMPLT: 
				case ByteCodeConstants.IF_ICMPGE:
				case ByteCodeConstants.IF_ICMPGT: 
				case ByteCodeConstants.IF_ICMPLE:	
					
				case ByteCodeConstants.IF_ACMPEQ:
				case ByteCodeConstants.IF_ACMPNE:
					
				case ByteCodeConstants.IFNONNULL:
				case ByteCodeConstants.IFNULL:
					
				case ByteCodeConstants.GOTO:
					jumpOffset = offset + 
										(short)( ((code[++offset] & 255) << 8) | 
							                      (code[++offset] & 255) );
					jumps[jumpOffset] = true;
					break;
				case ByteCodeConstants.JSR: 
					jumpOffset = offset + 
									(short)( ((code[++offset] & 255) << 8) | 
						                      (code[++offset] & 255) );
					offsetSet.add(jumpOffset);
					break;
				default:
					offset += 2;	
				}
				break;
				
			case 4:
				switch (opcode)
				{
				case ByteCodeConstants.GOTO_W:
					jumpOffset = offset + 
					                    ((code[++offset] & 255) << 24) | 
							            ((code[++offset] & 255) << 16) |
					                    ((code[++offset] & 255) << 8 ) |  
					                     (code[++offset] & 255);
					jumps[jumpOffset] = true;
					break;
				case ByteCodeConstants.JSR_W:
					jumpOffset = offset + 
				                     ((code[++offset] & 255) << 24) | 
						             ((code[++offset] & 255) << 16) |
				                     ((code[++offset] & 255) << 8 ) |  
				                      (code[++offset] & 255);
					offsetSet.add(jumpOffset);
					break;
				default:
					offset += 4;
				}
				break;				
			default:
				switch (opcode)
				{
				case ByteCodeConstants.TABLESWITCH:
					offset = ByteCodeUtil.TableSwitchOffset(code, offset);
					break;
				case ByteCodeConstants.LOOKUPSWITCH:
					offset = ByteCodeUtil.LookupSwitchOffset(code, offset);
					break;
				case ByteCodeConstants.WIDE:
					offset = ByteCodeUtil.WideOffset(code, offset);
					break;
				default:
					offset += ByteCodeConstants.NO_OF_OPERANDS[opcode];
				}
			}
		}
	}
	
	private static class CodeExceptionComparator 
		implements Comparator<CodeException>
	{
		public int compare(CodeException ce1, CodeException ce2)
		{
			if (ce1.handler_pc != ce2.handler_pc)
				return ce1.handler_pc - ce2.handler_pc;
			
			if (ce1.end_pc != ce2.end_pc)
				return ce1.end_pc - ce2.end_pc;
			
			return ce1.start_pc - ce2.start_pc;
		}
	}
}