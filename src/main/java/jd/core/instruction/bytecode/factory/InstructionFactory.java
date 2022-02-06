package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.instruction.Instruction;


public abstract class InstructionFactory 
{
	public abstract int create(
			ClassFile classFile, 
			Method method, 
			List<Instruction> list, 
			List<Instruction> listForAnalyze,
			Stack<Instruction> stack,
			byte[] code, 
			int offset, 
			int lineNumber,
			boolean[] jumps);
}
