package jd.core.instruction.bytecode.reconstructor;

import java.util.List;

import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.instruction.bytecode.instruction.DupLoad;
import jd.core.instruction.bytecode.instruction.GetField;
import jd.core.instruction.bytecode.instruction.GetStatic;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.LoadInstruction;
import jd.core.instruction.bytecode.instruction.PutField;
import jd.core.instruction.bytecode.instruction.PutStatic;
import jd.core.instruction.bytecode.instruction.StoreInstruction;
import jd.core.instruction.bytecode.visitor.CompareInstructionVisitor;


/*
 * Recontruction des operateurs d'assignation depuis les motifs :
 * 1) Operation sur les attributs de classes: 
 *    PutStatic(BinaryOperator(GetStatic(), ...))
 * 2) Operation sur les attributs d'instance: 
 *    PutField(objectref, BinaryOperator(GetField(objectref), ...))
 * 3) Operation sur les variables locales: 
 *    Store(BinaryOperator(Load(), ...))
 * 4) Operation sur les variables locales: 
 *    IStore(BinaryOperator(ILoad(), ...))
 * 5) Operation sur des tableaux: 
 *    ArrayStore(arrayref, indexref, 
 *               BinaryOperator(ArrayLoad(arrayref, indexref), ...))
 */
public class AssignmentOperatorReconstructor 
{
	public static void Reconstruct(List<Instruction> list)
	{
		int index = list.size();
		
		while (index-- > 0)
		{
			Instruction i = list.get(index);
			
			switch (i.opcode)
			{
			case ByteCodeConstants.PUTSTATIC:
				if (((PutStatic)i).valueref.opcode == 
						ByteCodeConstants.BINARYOP)
					index = ReconstructPutStaticOperator(list, index, i);
				break;
			case ByteCodeConstants.PUTFIELD:
				if (((PutField)i).valueref.opcode == 
						ByteCodeConstants.BINARYOP)
					index = ReconstructPutFieldOperator(list, index, i);
				break;
			case ByteCodeConstants.ISTORE:
				if (((StoreInstruction)i).valueref.opcode == 
						ByteCodeConstants.BINARYOP)
				{
					BinaryOperatorInstruction boi = (BinaryOperatorInstruction)
						((StoreInstruction)i).valueref;
					if (boi.value1.opcode == ByteCodeConstants.ILOAD) 
						index = ReconstructStoreOperator(list, index, i, boi);
				}
				break;
			case ByteCodeConstants.STORE:
				if (((StoreInstruction)i).valueref.opcode == 
						ByteCodeConstants.BINARYOP)
				{
					BinaryOperatorInstruction boi = (BinaryOperatorInstruction)
						((StoreInstruction)i).valueref;
					if (boi.value1.opcode == ByteCodeConstants.LOAD) 
						index = ReconstructStoreOperator(list, index, i, boi);
				}
				break;
			case ByteCodeConstants.ARRAYSTORE:
				if (((ArrayStoreInstruction)i).valueref.opcode == 
						ByteCodeConstants.BINARYOP)
					index = ReconstructArrayOperator(list, index, i);
				break;
			}
		}
	}

	/*
	 * PutStatic(BinaryOperator(GetStatic(), ...))
	 */
	private static int ReconstructPutStaticOperator(
		List<Instruction> list, int index, Instruction i)
	{
		PutStatic putStatic = (PutStatic)i;
		BinaryOperatorInstruction boi = 
			(BinaryOperatorInstruction)putStatic.valueref;
		
		if (boi.value1.opcode != ByteCodeConstants.GETSTATIC)
			return index;
		
		GetStatic getStatic = (GetStatic)boi.value1;
		
		if ((putStatic.lineNumber != getStatic.lineNumber) ||
			(putStatic.index != getStatic.index))
			return index;
		
		String newOperator = boi.operator + "=";
		
		list.set(index, new AssignmentInstruction(
			ByteCodeConstants.ASSIGNMENT, putStatic.offset,
			getStatic.lineNumber, boi.getPriority(), newOperator,
			getStatic, boi.value2));
		
		return index;		
	}
	
	/*
	 * PutField(objectref, BinaryOperator(GetField(objectref), ...))
	 */
	private static int ReconstructPutFieldOperator(
		List<Instruction> list, int index, Instruction i)
	{
		PutField putField = (PutField)i;
		BinaryOperatorInstruction boi = 
			(BinaryOperatorInstruction)putField.valueref;
		
		if (boi.value1.opcode != ByteCodeConstants.GETFIELD)
			return index;
		
		GetField getField = (GetField)boi.value1;
		CompareInstructionVisitor visitor = new CompareInstructionVisitor();

		if ((putField.lineNumber != getField.lineNumber) ||
			(putField.index != getField.index) ||
			!visitor.visit(putField.objectref, getField.objectref))
			return index;
		
		if (putField.objectref.opcode == ByteCodeConstants.DUPLOAD)
		{
			// Remove DupStore & DupLoad
			DupLoad dupLoad = (DupLoad)getField.objectref; 
			index = DeleteDupStoreInstruction(list, index, dupLoad);
			getField.objectref = dupLoad.dupStore.objectref;
		}
		
		String newOperator = boi.operator + "=";
		
		list.set(index, new AssignmentInstruction(
			ByteCodeConstants.ASSIGNMENT, putField.offset,
			getField.lineNumber, boi.getPriority(), newOperator,
			getField, boi.value2));
		
		return index;		
	}
	
	/*
	 * StoreInstruction(BinaryOperator(LoadInstruction(), ...))
	 */
	private static int ReconstructStoreOperator(
		List<Instruction> list, int index, 
		Instruction i, BinaryOperatorInstruction boi)
	{
		StoreInstruction si = (StoreInstruction)i;		
		LoadInstruction li = (LoadInstruction)boi.value1;
		
		if ((si.lineNumber != li.lineNumber) || (si.index != li.index))
			return index;
		
		String newOperator = boi.operator + "=";
		
		list.set(index, new AssignmentInstruction(
			ByteCodeConstants.ASSIGNMENT, si.offset,
			li.lineNumber, boi.getPriority(), newOperator,
			si, boi.value2));
		
		return index;		
	}
	
	/*
	 * ArrayStore(arrayref, indexref, 
	 *            BinaryOperator(ArrayLoad(arrayref, indexref), ...))
	 */
	private static int ReconstructArrayOperator(
		List<Instruction> list, int index, Instruction i)
	{
		ArrayStoreInstruction asi = (ArrayStoreInstruction)i;
		BinaryOperatorInstruction boi = (BinaryOperatorInstruction)asi.valueref;
		
		if (boi.value1.opcode != ByteCodeConstants.ARRAYLOAD)
			return index;
		
		ArrayLoadInstruction ali = (ArrayLoadInstruction)boi.value1;
		CompareInstructionVisitor visitor = new CompareInstructionVisitor();
		
		if ((asi.lineNumber != ali.lineNumber) ||
			!visitor.visit(asi.arrayref, ali.arrayref) || 
			!visitor.visit(asi.indexref, ali.indexref))
			return index;
		
		if (asi.arrayref.opcode == ByteCodeConstants.DUPLOAD)
		{
			// Remove DupStore & DupLoad
			DupLoad dupLoad = (DupLoad)ali.arrayref; 
			index = DeleteDupStoreInstruction(list, index, dupLoad);
			ali.arrayref = dupLoad.dupStore.objectref;
		}
		
		if (asi.indexref.opcode == ByteCodeConstants.DUPLOAD)
		{
			// Remove DupStore & DupLoad
			DupLoad dupLoad = (DupLoad)ali.indexref; 
			index = DeleteDupStoreInstruction(list, index, dupLoad);
			ali.indexref = dupLoad.dupStore.objectref;
		}
		
		String newOperator = boi.operator + "=";
		
		list.set(index, new AssignmentInstruction(
			ByteCodeConstants.ASSIGNMENT, asi.offset,
			ali.lineNumber, boi.getPriority(), newOperator,
			ali, boi.value2));
		
		return index;
	}
	
	private static int DeleteDupStoreInstruction(
		List<Instruction> list, int index, DupLoad dupLoad)
	{
		int indexTmp = index;
		
		while (indexTmp-- > 0)
		{
			Instruction i = list.get(indexTmp);

			if (dupLoad.dupStore == i)
			{
				list.remove(indexTmp);
				return --index;
			}
		}
		
		return index;
	}
}