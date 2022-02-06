package jd.core.instruction.fast.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;
import jd.core.instruction.bytecode.instruction.Instruction;

/**
 * list & while(true)
 */
public class FastDeclaration extends Instruction
{
	public int index;
	public Instruction instruction;
	
	public FastDeclaration(
		int opcode, int offset, int lineNumber, 
		int index, Instruction instruction)
	{
		super(opcode, offset, lineNumber);
		this.index = index;
		this.instruction = instruction;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables)
	{
		return null;
	}
}
