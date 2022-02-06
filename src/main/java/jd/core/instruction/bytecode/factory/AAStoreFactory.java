package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.AAStore;
import jd.core.instruction.bytecode.instruction.Instruction;


public class AAStoreFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final Instruction valueref = stack.pop();
		final Instruction index = stack.pop();
		final Instruction arrayref = stack.pop();
		final Instruction instruction = 
			new AAStore(opcode, offset, lineNumber, arrayref, index, valueref);
		
		list.add(instruction);
		listForAnalyze.add(instruction);
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
