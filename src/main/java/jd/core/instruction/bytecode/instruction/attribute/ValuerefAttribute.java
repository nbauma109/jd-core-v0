package jd.core.instruction.bytecode.instruction.attribute;

import jd.core.instruction.bytecode.instruction.Instruction;

public interface ValuerefAttribute 
{
	public Instruction getValueref();
	
	public void setValueref(Instruction valueref);
}
