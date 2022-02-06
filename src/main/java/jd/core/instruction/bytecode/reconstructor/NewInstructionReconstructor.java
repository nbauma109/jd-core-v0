package jd.core.instruction.bytecode.reconstructor;

import java.util.List;

import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.classfile.constant.ConstantNameAndType;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.DupLoad;
import jd.core.instruction.bytecode.instruction.DupStore;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.InvokeNew;
import jd.core.instruction.bytecode.instruction.Invokespecial;
import jd.core.instruction.bytecode.instruction.New;
import jd.core.instruction.bytecode.util.ReconstructorUtil;


/*
 * Recontruction de l'instruction 'new' depuis le motif :
 * DupStore( New(java/lang/Long) )
 * ...
 * Invokespecial(DupLoad, <init>, [ IConst_1 ])
 * ...
 * ??? DupLoad
 */
public class NewInstructionReconstructor 
{
	public static void Reconstruct(
			ClassFile classFile, List<Instruction> list)
	{
		for (int dupStoreIndex=0; dupStoreIndex<list.size(); dupStoreIndex++)
		{
			if (list.get(dupStoreIndex).opcode != ByteCodeConstants.DUPSTORE)
				continue;
			
			DupStore ds = (DupStore)list.get(dupStoreIndex);
			
			if (ds.objectref.opcode != ByteCodeConstants.NEW)
				continue;
			
			int invokespecialIndex = dupStoreIndex;
			final int length = list.size();
			
			while (++invokespecialIndex < length)
			{
				Instruction i = list.get(invokespecialIndex);

				if (i.opcode != ByteCodeConstants.INVOKESPECIAL)
					continue;
				
				Invokespecial is = (Invokespecial)i;

				if (is.objectref.opcode != ByteCodeConstants.DUPLOAD)
					continue;
				
				DupLoad dl = (DupLoad)is.objectref;
				
				if (dl.offset != ds.offset)
					continue;
				
				ConstantPool constants = classFile.getConstantPool();
				ConstantMethodref cmr = constants.getConstantMethodref(is.index);
				ConstantNameAndType cnat = 
					constants.getConstantNameAndType(cmr.name_and_type_index);
						
				if (cnat.name_index == constants.instanceConstructorIndex)
				{
					InvokeNew invokeNew = new InvokeNew(
						ByteCodeConstants.INVOKENEW, is.offset, is.lineNumber,  
						((New)ds.objectref).index, is.index, is.args);
					
					Instruction parentFound = ReconstructorUtil.ReplaceDupLoad(
							list, invokespecialIndex+1, ds, invokeNew);
					
					list.remove(invokespecialIndex);
					if (parentFound == null)
						list.set(dupStoreIndex, invokeNew);
					else
						list.remove(dupStoreIndex--);
					
					break;
				}
			}							
		}
	}
}
