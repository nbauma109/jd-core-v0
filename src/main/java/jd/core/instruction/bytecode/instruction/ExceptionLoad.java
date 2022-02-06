package jd.core.instruction.bytecode.instruction;

import jd.core.Constants;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

public class ExceptionLoad extends IndexInstruction 
{
	final public int exceptionNameIndex;
	
	public ExceptionLoad(
		int opcode, int offset, int lineNumber, int signatureIndex)
	{
		super(opcode, offset, lineNumber, Constants.INVALID_INDEX);
		this.exceptionNameIndex = signatureIndex;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		if ((constants == null) || (this.exceptionNameIndex == 0))
			return null;
		
		return constants.getConstantUtf8(this.exceptionNameIndex);
	}
}
