package jd.core.instruction.bytecode.visitor;

import java.util.HashMap;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.accessor.Accessor;
import jd.core.classfile.accessor.PutStaticAccessor;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.classfile.constant.ConstantNameAndType;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.Invokestatic;
import jd.core.instruction.bytecode.instruction.PutStatic;

/*
 * Replace 'TestInnerClass.access$0(1)' 
 * par 'TestInnerClass.test = 1'
 */
public class OuterPutStaticVisitor extends OuterGetStaticVisitor
{
	public OuterPutStaticVisitor(
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

		// One parameter ?
		if (cmr.getNbrOfParameters() != 1)
			return null;

		String className = constants.getConstantClassName(cmr.class_index);
		ClassFile classFile = this.innerClassesMap.get(className);
		if (classFile == null)
			return null;
		
		String name = 
			constants.getConstantUtf8(cnat.name_index);
		
		Accessor accessor = classFile.getAccessor(name, descriptor);
		
		if ((accessor == null) ||
			(accessor.tag != Constants.ACCESSOR_PUTSTATIC))
			return null;
		
		return (PutStaticAccessor)accessor;
	}
	
	protected Instruction newInstruction(Instruction i, Accessor a)
	{
		PutStaticAccessor psa = (PutStaticAccessor)a;		
		Invokestatic is = (Invokestatic)i;
		
		int nameIndex = this.constants.addConstantUtf8(psa.fieldName);
		int descriptorIndex = 
			this.constants.addConstantUtf8(psa.fieldDescriptor);
		int cnatIndex = 
			this.constants.addConstantNameAndType(nameIndex, descriptorIndex);		
		
		int classNameIndex = this.constants.addConstantUtf8(psa.className);	
		int classIndex = this.constants.addConstantClass(classNameIndex);
		
		int cfrIndex = 
			 this.constants.addConstantFieldref(classIndex, cnatIndex);
		
		Instruction valueref = is.args.remove(0);
		
		return new PutStatic(
			ByteCodeConstants.PUTSTATIC, i.offset, i.lineNumber, 
			cfrIndex, valueref);
	}
}
