package jd.core.instruction.bytecode.exception;


public class UnexpectedOpcodeException extends RuntimeException
{
	private static final long serialVersionUID = -3407799517256621265L;
	
	public UnexpectedOpcodeException(int opcode) 
	{ 
		super(String.valueOf(opcode)); 
	}
}
