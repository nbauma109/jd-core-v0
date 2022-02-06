package jd.core.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.classfile.ClassFile;
import jd.core.classfile.Method;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.DupStore;
import jd.core.instruction.bytecode.instruction.Instruction;


public class Dup2Factory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze, 
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		Instruction i1 = stack.pop();
		
		String signature = i1.getReturnedSignature(
				classFile.getConstantPool(), method.getLocalVariables());
		
		if ("J".equals(signature) || "D".equals(signature))
		{
			DupStore dupStore = new DupStore(
				ByteCodeConstants.DUPSTORE, offset, lineNumber, i1);
						
			list.add(dupStore);	
			stack.push(dupStore.getDupLoad1());
			stack.push(dupStore.getDupLoad2());
		}
		else
		{
			Instruction i2 = stack.pop();
			
			DupStore dupStore1 = new DupStore(
				ByteCodeConstants.DUPSTORE, offset, lineNumber, i1); 
			DupStore dupStore2 = new DupStore(
				ByteCodeConstants.DUPSTORE, offset, lineNumber, i2);
					
			list.add(dupStore1);	
			list.add(dupStore2);			
			stack.push(dupStore2.getDupLoad1());
			stack.push(dupStore1.getDupLoad1());
			stack.push(dupStore2.getDupLoad2());
			stack.push(dupStore1.getDupLoad2());
		}	
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}
