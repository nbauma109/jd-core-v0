package jd.core.instruction.bytecode.visitor;

import java.util.HashMap;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.accessor.Accessor;
import jd.core.classfile.accessor.InvokeMethodAccessor;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.classfile.constant.ConstantNameAndType;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.Invokespecial;
import jd.core.instruction.bytecode.instruction.Invokestatic;
import jd.core.instruction.bytecode.instruction.Invokevirtual;

/*
 * Replace 'EntitlementFunctionLibrary.access$000()' 
 * par 'EntitlementFunctionLibrary.kernelId'
 */
public class OuterInvokeMethodVisitor extends OuterGetStaticVisitor
{
	public OuterInvokeMethodVisitor(
		HashMap<String, ClassFile> innerClassesMap, ConstantPool constants)
	{
		super(innerClassesMap, constants);
	}

	protected Accessor match(Instruction i)
	{
		if (i.opcode != ByteCodeConstants.INVOKESTATIC)		
			return null; 
		
		Invokestatic is = (Invokestatic)i;
		ConstantMethodref cmr = 
			constants.getConstantMethodref(is.index);
		ConstantNameAndType cnat = 
			constants.getConstantNameAndType(cmr.name_and_type_index);
		String descriptor = 
			constants.getConstantUtf8(cnat.descriptor_index);

		// Zero parameter ?
		if (descriptor.charAt(1) == ')')
			return null;

		String className = constants.getConstantClassName(cmr.class_index);
		ClassFile classFile = this.innerClassesMap.get(className);
		if (classFile == null)
			return null;
		
		String name = 
			constants.getConstantUtf8(cnat.name_index);
		
		Accessor accessor = classFile.getAccessor(name, descriptor);
		
		if ((accessor == null) ||
			(accessor.tag != Constants.ACCESSOR_INVOKEMETHOD))
			return null;
		
		return accessor;
	}
	
	protected Instruction newInstruction(Instruction i, Accessor a)
	{
		InvokeMethodAccessor ima = (InvokeMethodAccessor)a;
		Invokestatic is = (Invokestatic)i;
		
		int nameIndex = this.constants.addConstantUtf8(ima.methodName);
		int descriptorIndex = 
			this.constants.addConstantUtf8(ima.methodDescriptor);
		int cnatIndex = 
			this.constants.addConstantNameAndType(nameIndex, descriptorIndex);
		
		int classNameIndex = this.constants.addConstantUtf8(ima.className);	
		int classIndex = this.constants.addConstantClass(classNameIndex);
		
		int cmrIndex = constants.addConstantMethodref(
			classIndex, cnatIndex, 
			ima.listOfParameterSignatures, ima.returnedSignature);
		
		Instruction objectref = is.args.remove(0);
				
		switch (ima.methodOpcode)
		{
		case ByteCodeConstants.INVOKESPECIAL:
			return new Invokespecial(
				ByteCodeConstants.INVOKESPECIAL, i.offset, i.lineNumber, 
				cmrIndex, objectref, is.args);
		case ByteCodeConstants.INVOKEVIRTUAL:
			return new Invokevirtual(
				ByteCodeConstants.INVOKEVIRTUAL, i.offset, i.lineNumber, 
				cmrIndex, objectref, is.args);
		default:
			return i;
		}
	}
}
