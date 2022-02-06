package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.DConst;
import jd.core.instruction.bytecode.instruction.Instruction;


public class DConstFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final int index = opcode - ByteCodeConstants.DCONST_0;
		
		stack.push(new DConst(
			ByteCodeConstants.DCONST, offset, lineNumber, index));
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}