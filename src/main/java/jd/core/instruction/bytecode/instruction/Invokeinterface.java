package jd.core.instruction.bytecode.instruction;

import java.util.List;


public class Invokeinterface extends InvokeNoStaticInstruction 
{
	public Invokeinterface(
			int opcode, int offset, int lineNumber, int index, int count, 
			Instruction objectref, List<Instruction> args)
	{
		super(opcode, offset, lineNumber, index, objectref, args);
	}
}
