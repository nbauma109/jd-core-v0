package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;


public class LoadInstruction extends IndexInstruction 
{
	private String signature;
	
	public LoadInstruction(
		int opcode, int offset, int lineNumber, int index, String signature)
	{
		super(opcode, offset, lineNumber, index);
		this.signature = signature;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{		
		return this.signature;
	}
}
