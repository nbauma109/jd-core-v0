package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.instruction.bytecode.instruction.Instruction;


public class BinaryOperatorFactory extends InstructionFactory
{
	protected int priority;
	protected String signature;
	protected String operator;
	
	public BinaryOperatorFactory(
			int priority, String signature, String operator)
	{
		this.priority = priority;
		this.signature = signature;
		this.operator = operator;
	}
	
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final Instruction i2 = stack.pop();
		final Instruction i1 = stack.pop();	
		
		final Instruction instruction = new BinaryOperatorInstruction(
			ByteCodeConstants.BINARYOP, offset, lineNumber, this.priority, 
			this.signature, this.operator, i1, i2);
			
		stack.push(instruction);
		listForAnalyze.add(instruction);
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
