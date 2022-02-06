package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.AThrow;
import jd.core.instruction.bytecode.instruction.Instruction;


public class AThrowFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;

		list.add(new AThrow(opcode, offset, lineNumber, stack.pop()));
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
