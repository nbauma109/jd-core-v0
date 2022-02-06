package jd.core.instruction.fast.visitor;

import java.util.HashSet;

import jd.core.Preferences;
import jd.core.classfile.ClassFile;
import jd.core.classfile.LocalVariable;
import jd.core.classfile.LocalVariables;
import jd.core.classfile.writer.SignatureWriter;
import jd.core.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.instruction.bytecode.instruction.IfCmp;
import jd.core.instruction.bytecode.instruction.IfInstruction;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.visitor.ByteCodeSourceWriterVisitor;
import jd.core.instruction.fast.FastConstants;
import jd.core.instruction.fast.instruction.FastDeclaration;
import jd.core.printer.Printer;
import jd.core.util.ReferenceMap;



public class FastSourceWriterVisitor 
	extends ByteCodeSourceWriterVisitor
{
	public FastSourceWriterVisitor(
			HashSet<String> keywordSet, Preferences preferences, Printer spw, 
			ReferenceMap referenceMap, ClassFile classFile, 
			int methodAccessFlags, LocalVariables localVariables)
	{
		super(
			keywordSet, preferences, spw, referenceMap, classFile, 
			methodAccessFlags, localVariables);
	}

	public void visit(Instruction instruction)
	{
		switch (instruction.opcode)
		{
		case FastConstants.IF:
			writeIfTest((IfInstruction)instruction);
			break;
		case FastConstants.IFCMP:
			writeIfCmpTest((IfCmp)instruction);
			break;
		case FastConstants.IFXNULL:
			writeIfXNullTest((IfInstruction)instruction);
			break;
		case FastConstants.COMPLEXIF:
			writeComplexConditionalBranchInstructionTest((ComplexConditionalBranchInstruction)instruction);
			break;
		case FastConstants.DECLARE:
			WriteDeclaration((FastDeclaration)instruction);
			break;
		default:
			super.visit(instruction);
		}
	}
	
	private void WriteDeclaration(FastDeclaration fd)
	{
		LocalVariable lv = 
			localVariables.getLocalVariableWithIndexAndOffset(fd.index, fd.offset);
		
		if (lv == null)
		{
			if (fd.instruction == null)
				spw.print(
					Instruction.UNKNOWN_LINE_NUMBER, "???");
			else
				visit(fd.instruction);
		}
		else
		{
			String signature = this.constants.getConstantUtf8(lv.signature_index);
			
			int lineNumber = fd.lineNumber;
			
			spw.print(
				lineNumber, 
				SignatureWriter.WriteSimpleSignature(
					referenceMap, classFile, signature));
			spw.print(lineNumber, ' ');
			
			if (fd.instruction == null)
				spw.print(
					lineNumber, constants.getConstantUtf8(lv.name_index));
			else
				visit(fd.instruction);
		}
	}
}
