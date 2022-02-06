package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;


public class AThrow extends Instruction 
{
	public Instruction value;

	public AThrow(
		int opcode, int offset, int lineNumber, Instruction value)
	{
		super(opcode, offset, lineNumber);
		this.value = value;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{		
		return this.value.getReturnedSignature(constants, localVariables);
	}
}
