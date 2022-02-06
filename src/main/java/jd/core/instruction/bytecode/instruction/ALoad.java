package jd.core.instruction.bytecode.instruction;

import jd.core.Constants;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariable;
import jd.core.classfile.LocalVariables;

public class ALoad extends LoadInstruction 
{
	public ALoad(int opcode, int offset, int lineNumber, int index)
	{
		super(opcode, offset, lineNumber, index, null);
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		if ((constants == null) || (localVariables == null))
			return null;
		
		LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(this.index, this.offset);
		
		if ((lv != null) && (lv.signature_index > 0))
			return constants.getConstantUtf8(lv.signature_index);
		
		return Constants.INTERNAL_OBJECT_SIGNATURE;
	}
}