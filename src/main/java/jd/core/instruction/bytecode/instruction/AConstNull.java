package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

public class AConstNull extends Instruction 
{
	public AConstNull(int opcode, int offset, int lineNumber)
	{
		super(opcode, offset, lineNumber);
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		return null;
	}
}
