package jd.core.instruction.fast.reconstructor;

import java.util.ArrayList;
import java.util.List;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.Field;
import jd.core.classfile.Method;
import jd.core.classfile.constant.ConstantFieldref;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.classfile.constant.ConstantNameAndType;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.ALoad;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.Invokespecial;
import jd.core.instruction.bytecode.instruction.PutField;
import jd.core.instruction.bytecode.visitor.CompareInstructionVisitor;
import jd.core.instruction.fast.FastConstants;
import jd.core.instruction.fast.visitor.SearchInstructionByOpcodeVisitor;


public class InitInstanceFieldsReconstructor 
{
	public static void Reconstruct(ClassFile classFile)
	{
		ArrayList<PutField> putFieldList = new ArrayList<PutField>();
    	ConstantPool constants = classFile.getConstantPool();

    	Method[] methods = classFile.getMethods();
		int methodIndex = methods.length;
		Method putFieldListMethod = null;
		
		// Recherche du dernier constructeur ne faisait pas appel a 'this(...)'
		while (methodIndex > 0)
		{
	    	final Method method = methods[--methodIndex];
	    	
	    	if (((method.access_flags & (Constants.ACC_SYNTHETIC|Constants.ACC_BRIDGE)) != 0) ||
	    		(method.getCode() == null) ||
	    		(method.getFastNodes() == null) ||
	    		(method.containsError() == true) ||
	    		(method.name_index != constants.instanceConstructorIndex))
	    		continue;
			
			List<Instruction> list = method.getFastNodes();
	    	if (list == null)
	    		continue;
	    	
			int length = list.size();
			
			if (length > 0)
			{
				int j = GetSuperCallIndex(classFile, constants, list);
				
				if (j < 0)
					continue;
					
				j++;
				
				int lineNumberBefore = (j > 0) ? 
					list.get(j-1).lineNumber : Instruction.UNKNOWN_LINE_NUMBER;
				Instruction instruction = null;
					
				// Store init values
				while (j < length)
				{
					instruction = list.get(j++);
					if (instruction.opcode != ByteCodeConstants.PUTFIELD)
						break;
					
					PutField putField = (PutField)instruction;
					ConstantFieldref cfr = constants.getConstantFieldref(putField.index);

					if ((cfr.class_index != classFile.getThisClassIndex()) ||
						(putField.objectref.opcode != ByteCodeConstants.ALOAD))
						break;
					
					ALoad aLaod = (ALoad)putField.objectref;
					if (aLaod.index != 0)
						break;
					
					Instruction valueInstruction = 
						SearchInstructionByOpcodeVisitor.visit(
								putField.valueref, ByteCodeConstants.ALOAD);
					if ((valueInstruction != null) && 
						(((ALoad)valueInstruction).index != 0))
						break;
					if (SearchInstructionByOpcodeVisitor.visit(
							putField.valueref, ByteCodeConstants.LOAD) != null)
						break;
					if (SearchInstructionByOpcodeVisitor.visit(
							putField.valueref, ByteCodeConstants.ILOAD) != null)
						break;

					putFieldList.add(putField);
					putFieldListMethod = method;
				}
				
				// Filter list of 'PUTFIELD'
				if ((lineNumberBefore != Instruction.UNKNOWN_LINE_NUMBER) && 
					(instruction != null))
				{
					int i = putFieldList.size();
					int lineNumberAfter = instruction.lineNumber;

					// Si l'instruction qui suit la serie de 'PUTFIELD' est une
					// 'RETURN' ayant le meme numero de ligne que le dernier
					// 'PUTFIELD', le constructeur est synthetique et ne sera
					// pas filtre.
					if ((instruction.opcode != ByteCodeConstants.RETURN) ||
						(j != length) || (i == 0) ||
						(lineNumberAfter != putFieldList.get(i-1).lineNumber))
					{					
						while (i-- > 0)
						{
							int lineNumber = putFieldList.get(i).lineNumber;
							
							if ((lineNumberBefore <= lineNumber) &&
								(lineNumber <= lineNumberAfter))
							{
								// Remove 'PutField' instruction if it used in code 
								// block of constructor
								putFieldList.remove(i);
							}
						}
					}
				}
			}
			
			break;
		}
	    
		// Filter list
		while (methodIndex > 0)
		{
	    	final Method method = methods[--methodIndex];
	    	
	    	if (((method.access_flags & 
	    			(Constants.ACC_SYNTHETIC|Constants.ACC_BRIDGE)) != 0))
	    		continue;
	    	if (method.getCode() == null)
	    		continue;
			if (method.name_index != constants.instanceConstructorIndex)
				continue;
			
			List<Instruction> list = method.getFastNodes();
			int length = list.size();
			
			if (length > 0)
			{
				// Filter init values
				int j = GetSuperCallIndex(classFile, constants, list);
				
				if (j < 0)
					continue;
				
				int firstPutFieldIndex = j + 1;
				int putFieldListLength = putFieldList.size();

				// If 'putFieldList' is more loonger than 'list', 
				// remove extra 'putField'.
				while (firstPutFieldIndex+putFieldListLength > length)
					putFieldList.remove(--putFieldListLength);					

				for (int i=0; i<putFieldListLength; i++)
				{
					Instruction initFieldInstruction = putFieldList.get(i);
					Instruction instruction = list.get(firstPutFieldIndex+i);
					CompareInstructionVisitor visitor = 
						new CompareInstructionVisitor();
					
					if ((initFieldInstruction.lineNumber != instruction.lineNumber) ||
						!visitor.visit(initFieldInstruction, instruction))
					{
						while (i < putFieldListLength)
							putFieldList.remove(--putFieldListLength);
						break;
					}					
				}
			}
		}
		
		// Setup initial values
		int putFieldListLength = putFieldList.size();
		
		if (putFieldListLength > 0)
		{		
			Field[] fields = classFile.getFields();
			int fieldLength = fields.length;
			int putFieldListIndex = putFieldListLength;
				
			while (putFieldListIndex-- > 0)
			{
				PutField putField = putFieldList.get(putFieldListIndex);			
				ConstantFieldref cfr = 
					constants.getConstantFieldref(putField.index);
				ConstantNameAndType cnat = 
					constants.getConstantNameAndType(cfr.name_and_type_index);
				int fieldIndex;
			
				for (fieldIndex=0; fieldIndex<fieldLength; fieldIndex++)
				{
					Field field = fields[fieldIndex];
				
					if ((cnat.name_index == field.name_index) &&
						(cnat.descriptor_index == field.descriptor_index) &&
						((field.access_flags & Constants.ACC_STATIC) == 0))	
					{
						// Field found
						Instruction valueref = putField.valueref;
						field.setValueAndLocalVariables(
							valueref, putFieldListMethod.getLocalVariables());
						if (valueref.opcode == FastConstants.NEWANDINITARRAY)
							valueref.opcode = FastConstants.INITARRAY;
						break;
					}
				}
				
				if (fieldIndex == fieldLength)
				{
					// Field not found
					// Remove putField not use to initialize fields
					putFieldList.remove(putFieldListIndex);
					putFieldListLength--;					
				}
			}
			
			if (putFieldListLength > 0)
			{
				// Remove instructions from constructors
				methodIndex = methods.length;
				
				while (methodIndex-- > 0)
				{
			    	final Method method = methods[methodIndex];
			    	
			    	if (((method.access_flags & 
			    			(Constants.ACC_SYNTHETIC|Constants.ACC_BRIDGE)) != 0))
			    		continue;
			    	if (method.getCode() == null)
			    		continue;
					if (method.name_index != constants.instanceConstructorIndex)
						continue;
					
					List<Instruction> list = method.getFastNodes();
					int length = list.size();
					
					if (length > 0)
					{
						// Remove instructions
						putFieldListIndex = 0;
						int putFieldIndex = putFieldList.get(putFieldListIndex).index;
						
						for (int index=0; index<length; index++)
						{
							Instruction instruction = list.get(index);						
							if (instruction.opcode != ByteCodeConstants.PUTFIELD)
								continue;
							
							PutField putField = (PutField)instruction;
							if (putField.index != putFieldIndex)
								continue;
							
							ConstantFieldref cfr = 
								constants.getConstantFieldref(putField.index);
							if ((cfr.class_index != classFile.getThisClassIndex()) ||
								(putField.objectref.opcode != ByteCodeConstants.ALOAD))
								continue;
							
							ALoad aLaod = (ALoad)putField.objectref;
							if (aLaod.index != 0)
								continue;
							
							list.remove(index--);
							
							if (++putFieldListIndex >= putFieldListLength)
								break;
							putFieldIndex = 
								putFieldList.get(putFieldListIndex).index;
							
						}
					}
				}
			}
		}
	}	
	
	private static int GetSuperCallIndex(
		ClassFile classFile, ConstantPool constants, List<Instruction> list)
	{
		int length = list.size();
		
		for (int i=0; i<length; i++)
		{
			Instruction instruction = list.get(i);
			
			if (instruction.opcode != ByteCodeConstants.INVOKESPECIAL)
				continue;
				
			Invokespecial is = (Invokespecial)instruction;
			
			if ((is.objectref.opcode != ByteCodeConstants.ALOAD) ||
				(((ALoad)is.objectref).index != 0))
				continue;
			
			ConstantMethodref cmr = constants.getConstantMethodref(is.index);			
			ConstantNameAndType cnat = 
				constants.getConstantNameAndType(cmr.name_and_type_index);
			
			if (cnat.name_index != constants.instanceConstructorIndex)
				continue;
			
			if (cmr.class_index == classFile.getThisClassIndex())
				return -1;
			
			return i;
		}
		
		return -1;
	}
}
