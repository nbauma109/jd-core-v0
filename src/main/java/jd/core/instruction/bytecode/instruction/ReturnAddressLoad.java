package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

/*
 * Pseudo instruction plac�e en debut de sous procedure. Lors de l'execution, 
 * cette adresse est normalement plac�e sur la pile par l'instruction JSR.
 */
public class ReturnAddressLoad extends Instruction 
{
	public ReturnAddressLoad(int opcode, int offset, int lineNumber)
	{
		super(opcode, offset, lineNumber);
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		return null;
	}
}