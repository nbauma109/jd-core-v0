package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;


public class ConditionalBranchInstruction extends BranchInstruction 
{
	public int cmp;
	
	public ConditionalBranchInstruction(
		int opcode, int offset, int lineNumber, int cmp, int branch)
	{
		super(opcode, offset, lineNumber, branch);
		this.cmp = cmp;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		return "Z";
	}
}
