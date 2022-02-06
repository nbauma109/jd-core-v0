package jd.core.exception;


public class ClassFormatException extends RuntimeException
{
	private static final long serialVersionUID = -3407799517256621265L;

	public ClassFormatException() 
	{ 
		super(); 
	}
	
	public ClassFormatException(String s) 
	{ 
		super(s); 
	}
}
