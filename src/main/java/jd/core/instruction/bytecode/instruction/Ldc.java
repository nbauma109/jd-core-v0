package jd.core.instruction.bytecode.instruction;

import jd.core.Constants;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;
import jd.core.classfile.constant.Constant;
import jd.core.classfile.constant.ConstantClass;

public class Ldc extends LdcInstruction 
{
	public Ldc(int opcode, int offset, int lineNumber, int index)
	{
		super(opcode, offset, lineNumber, index);
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		if (constants == null)
			return null;
		
		Constant c = constants.get(this.index);
		
		if (c == null)
			return null;
		
		switch (c.tag)
		{
		case Constants.CONSTANT_Float:
			return "F";
		case Constants.CONSTANT_Integer:
			return "I";
		case Constants.CONSTANT_String:
			return Constants.INTERNAL_STRING_SIGNATURE;
		case Constants.CONSTANT_Class:
			{
				int index = ((ConstantClass)c).name_index;				
				String signature = constants.getConstantUtf8(index);
				
				if (signature.charAt(0) != '[')
					signature = 'L' + signature + ';';
				
				return signature;
			}
		default:
			return null;
		}
	}	
}
