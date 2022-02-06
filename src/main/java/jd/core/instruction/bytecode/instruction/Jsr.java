package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

public class Jsr extends Instruction 
{
	final public int value;

	public Jsr(int opcode, int offset, int lineNumber, int value)
	{
		super(opcode, offset, lineNumber);
		this.value = value;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		return null;
	}
}
