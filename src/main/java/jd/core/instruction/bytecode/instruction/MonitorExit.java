package jd.core.instruction.bytecode.instruction;

import jd.core.classfile.ConstantPool;
import jd.core.classfile.LocalVariables;

public class MonitorExit extends Instruction 
{
	public Instruction objectref;

	public MonitorExit(
		int opcode, int offset, int lineNumber, Instruction objectref)
	{
		super(opcode, offset, lineNumber);
		this.objectref = objectref;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{		
		return null;
	}
}
