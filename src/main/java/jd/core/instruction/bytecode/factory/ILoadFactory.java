package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.ILoad;
import jd.core.instruction.bytecode.instruction.Instruction;


public class ILoadFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,  
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		int index;
		
		if (opcode == ByteCodeConstants.ILOAD)
			index = code[offset+1] & 255;
		else
			index = opcode - ByteCodeConstants.ILOAD_0;
		
		final Instruction instruction = 
			new ILoad(ByteCodeConstants.ILOAD, offset, lineNumber, index);		
		
		stack.push(instruction);		
		listForAnalyze.add(instruction);
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
