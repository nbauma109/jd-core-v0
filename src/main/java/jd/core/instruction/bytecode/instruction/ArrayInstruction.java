package jd.core.instruction.bytecode.instruction;


public abstract class ArrayInstruction extends Instruction 
{
	public Instruction arrayref;
	
	public ArrayInstruction(
		int opcode, int offset, int lineNumber, Instruction arrayref)
	{
		super(opcode, offset, lineNumber);
		this.arrayref = arrayref;
	}
}
