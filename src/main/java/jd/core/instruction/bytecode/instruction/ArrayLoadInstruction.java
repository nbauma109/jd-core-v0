package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;


public class ArrayLoadInstruction extends ArrayInstruction 
{
	public String signature;
	public Instruction indexref;
	
	public ArrayLoadInstruction(
			int opcode, int offset, int lineNumber, Instruction arrayref, 
			Instruction indexref, String signature)
	{
		super(opcode, offset, lineNumber, arrayref);
		this.indexref = indexref;
		this.signature = signature;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{		
		return this.signature;
	}
}
