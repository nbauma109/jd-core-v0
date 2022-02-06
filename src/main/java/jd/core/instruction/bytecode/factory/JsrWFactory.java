package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.Jsr;


public class JsrWFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze, 
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final int value  = 
			((code[offset+1] & 255) << 24) | ((code[offset+2] & 255) << 16) |
		    ((code[offset+3] & 255) <<  8) | (code[offset+4] & 255);

		list.add(new Jsr(ByteCodeConstants.JSR, offset, lineNumber, value));
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
