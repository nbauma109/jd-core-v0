package jd.core.instruction.bytecode.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.exception.InvalidParameterException;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.Invokestatic;


public class InvokestaticFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,  
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final int index = ((code[offset+1] & 255) << 8) | (code[offset+2] & 255);
		
		ConstantMethodref cmr = 
			classFile.getConstantPool().getConstantMethodref(index);
		if (cmr == null)
			throw new InvalidParameterException(
					"Invalid ConstantMethodref index");
		
		int nbrOfParameters = cmr.getNbrOfParameters();
		ArrayList<Instruction> args = new ArrayList<Instruction>(nbrOfParameters);
		
		for (int i=nbrOfParameters; i>0; --i)
			args.add(stack.pop());
		
		Collections.reverse(args);
		
		final Instruction instruction = new Invokestatic(
				opcode, offset, lineNumber, index, args);
			
		if (cmr.returnAResult())
			stack.push(instruction);
		else
			list.add(instruction);
		
		listForAnalyze.add(instruction);
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
