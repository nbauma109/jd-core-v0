package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;
import jd.core.classfile.constant.ConstantFieldref;
import jd.core.classfile.constant.ConstantNameAndType;

public class GetStatic extends IndexInstruction
{
	public GetStatic(int opcode, int offset, int lineNumber, int index)
	{
		super(opcode, offset, lineNumber, index);
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{
		if (constants == null)
			return null;
		
		ConstantFieldref cfr = constants.getConstantFieldref(this.index);
		if (cfr == null)
			return null;
		
		ConstantNameAndType cnat = 
			constants.getConstantNameAndType(cfr.name_and_type_index);
		if (cnat == null)
			return null;
		
		return constants.getConstantUtf8(cnat.descriptor_index);
	}
}
