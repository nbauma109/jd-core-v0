package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

public class ANewArray extends IndexInstruction
{
	public Instruction dimension;
	
	public ANewArray(
		int opcode, int offset, int lineNumber, 
		int index, Instruction dimension)
	{
		super(opcode, offset, lineNumber, index);
		this.dimension = dimension;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		if (constants == null)
			return null;
		
		return "[L" + constants.getConstantClassName(this.index) + ';';
	}
}
