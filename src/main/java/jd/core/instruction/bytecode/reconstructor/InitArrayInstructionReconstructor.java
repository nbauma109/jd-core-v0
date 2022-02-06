package jd.core.instruction.bytecode.reconstructor;

import java.util.ArrayList;
import java.util.List;

import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.instruction.bytecode.instruction.BIPush;
import jd.core.instruction.bytecode.instruction.DupStore;
import jd.core.instruction.bytecode.instruction.IConst;
import jd.core.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.NewArray;
import jd.core.instruction.bytecode.instruction.SIPush;
import jd.core.instruction.bytecode.util.ReconstructorUtil;


/*
 * Recontruction des initialisation de tableaux depuis le motif :
 * DupStore0 ( NewArray | ANewArray ... )
 * ?AStore ( DupLoad0, index=0, value )
 * DupStore1 ( DupLoad0 )
 * ?AStore ( DupLoad1, index=1, value )
 * DupStore2 ( DupLoad1 )
 * ?AStore ( DupLoad2, index=2, value )
 * ...
 * ???( DupLoadN )
 * 
 * Cette operation doit etre executee avant 'AssignmentInstructionReconstructor'.
 */
public class InitArrayInstructionReconstructor 
{
	public static void Reconstruct(List<Instruction> list)
	{
		for (int index=list.size()-1; index>=0; --index)
		{
			Instruction i = list.get(index);
			
			if (i.opcode != ByteCodeConstants.DUPSTORE)
				continue;

			DupStore dupStore = (DupStore)i;
			int opcode = dupStore.objectref.opcode;
				
			if ((opcode != ByteCodeConstants.NEWARRAY) && 
				(opcode != ByteCodeConstants.ANEWARRAY))
				continue;
			
			ReconstructAInstruction(list, index, dupStore);
		}	
	}

	private static void ReconstructAInstruction(
		List<Instruction> list, int index, DupStore dupStore)
	{
		// 1er DupStore trouv�
		final int length = list.size();
		int firstDupStoreIndex = index;
		DupStore lastDupStore = dupStore;
		int arrayIndex = 0;
		List<Instruction> values = new ArrayList<Instruction>();
		
		while (++index < length)
		{
			Instruction i = list.get(index);				

			// Recherche de ?AStore ( DupLoad, index, value )
			if ((i.opcode != ByteCodeConstants.AASTORE) &&
				(i.opcode != ByteCodeConstants.ARRAYSTORE))
				break;
			
			ArrayStoreInstruction asi = (ArrayStoreInstruction)i;

			if ((asi.arrayref.opcode != ByteCodeConstants.DUPLOAD) || 
				(asi.arrayref.offset != lastDupStore.offset))
				break;

			// Si les premieres cases d'un tableau ont pour valeur 0, elles
			// ne sont pas initialisee ! La boucle suivante reconstruit 
			// l'initialisation des valeurs 0.
			int indexOfArrayStoreInstruction = getArrayIndex(asi.indexref);			
			while (indexOfArrayStoreInstruction > arrayIndex)
			{
				values.add(new IConst(
					ByteCodeConstants.ICONST, asi.offset, asi.lineNumber, 0));
				arrayIndex++;
			}
			
			values.add(asi.valueref);
			arrayIndex++;				
			
			// Recherche de DupStoreM( DupLoadN )
			if (++index >= length)
				break;
			
			i = list.get(index);
			
			if (i.opcode != ByteCodeConstants.DUPSTORE)
				break;
			
			DupStore nextDupStore = (DupStore)i;
			
			if ((nextDupStore.objectref.opcode != ByteCodeConstants.DUPLOAD) ||
				(nextDupStore.objectref.offset != lastDupStore.offset))
				break;
			
			lastDupStore = nextDupStore;
		}		
		
		if (values.size() > 0)
		{
			// Instanciation d'une instruction InitArrayInstruction
			InitArrayInstruction iai = new InitArrayInstruction(
				ByteCodeConstants.NEWANDINITARRAY, lastDupStore.offset, 
				dupStore.lineNumber, dupStore.objectref, values);
			
			// Recherche de l'instruction 'DupLoad' suivante
			Instruction parent = ReconstructorUtil.ReplaceDupLoad(
				list, index, lastDupStore, iai);
			
			if (parent != null)
				switch (parent.opcode)
				{
				case ByteCodeConstants.AASTORE:
					iai.opcode = ByteCodeConstants.INITARRAY;
				}
			
			// Retrait des instructions de la liste
			while (firstDupStoreIndex < index)
				list.remove(--index);
			
			// Initialisation des types de constantes entieres	
			if (iai.newArray.opcode == ByteCodeConstants.NEWARRAY)
			{
				NewArray na = (NewArray)iai.newArray;
				
				switch (na.type)
				{
				case ByteCodeConstants.T_BOOLEAN: 
					SetContantTypes("Z", iai.values);
					break;
				case ByteCodeConstants.T_CHAR:
					SetContantTypes("C", iai.values);
					break;
				case ByteCodeConstants.T_BYTE:
					SetContantTypes("B", iai.values);
					break;
				case ByteCodeConstants.T_SHORT:
					SetContantTypes("S", iai.values);
					break;
				case ByteCodeConstants.T_INT:
					SetContantTypes("I", iai.values);
					break;
				}
			}
		}
	}
		
	private static void SetContantTypes(
		String signature, List<Instruction> values)
	{
		final int length = values.size();

		for (int i=0; i<length; i++)
		{
			Instruction value = values.get(i);

			switch (value.opcode)
			{
			case ByteCodeConstants.BIPUSH:
			case ByteCodeConstants.ICONST:
			case ByteCodeConstants.SIPUSH:
				((IConst)value).setReturnedSignature(signature);
				break;
			}
		}
	}
	
	private static int getArrayIndex(Instruction i)
	{
		switch (i.opcode)
		{
		case ByteCodeConstants.ICONST:
			return ((IConst)i).value;
		case ByteCodeConstants.BIPUSH:
			return ((BIPush)i).value;
		case ByteCodeConstants.SIPUSH:
			return ((SIPush)i).value;
		default:
			return -1;
		}
	}
}
