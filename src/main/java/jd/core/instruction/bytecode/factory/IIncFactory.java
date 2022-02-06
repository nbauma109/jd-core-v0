package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.IInc;
import jd.core.instruction.bytecode.instruction.ILoad;
import jd.core.instruction.bytecode.instruction.IncInstruction;
import jd.core.instruction.bytecode.instruction.Instruction;


public class IIncFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,  
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final int index = code[offset+1];
		final int count = code[offset+2];
		
		Instruction instruction;
		
		if (stack.isEmpty() || jumps[offset])
		{		
			list.add(instruction = 
				new IInc(opcode, offset, lineNumber, index, count));
		}
		else
		{
			instruction = stack.lastElement();
			
			if ((instruction.opcode == ByteCodeConstants.ILOAD) && 
					(((ILoad)instruction).index == index)) 
			{
				// Replace IInc instruction by a post-inc instruction
				stack.pop();
				stack.push(instruction = new IncInstruction(
					ByteCodeConstants.POSTINC, offset, 
					lineNumber, instruction, count));
			}
			else
			{
				list.add(instruction = 
					new IInc(opcode, offset, lineNumber, index, count));
			}
		}
		
		listForAnalyze.add(instruction);
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
