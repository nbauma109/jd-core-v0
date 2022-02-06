package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

public class IStore extends StoreInstruction 
{
	public IStore(
		int opcode, int offset, int lineNumber, 
		int index, Instruction objectref)
	{
		super(opcode, offset, lineNumber, index, null, objectref);
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		return this.valueref.getReturnedSignature(constants, localVariables);
	}
}