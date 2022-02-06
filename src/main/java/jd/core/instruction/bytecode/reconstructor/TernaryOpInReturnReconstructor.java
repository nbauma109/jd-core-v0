package jd.core.instruction.bytecode.reconstructor;

import java.util.List;

import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.analyzer.ComparisonInstructionAnalyzer;
import jd.core.instruction.bytecode.instruction.BranchInstruction;
import jd.core.instruction.bytecode.instruction.Goto;
import jd.core.instruction.bytecode.instruction.IConst;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.ReturnInstruction;


/*
 * Recontruction de l'instruction 'return (b1 == 1);' depuis la sequence : 
 * 46: if (b1 == 1)
 *   46: return true;
 * 48: return false;
 */
public class TernaryOpInReturnReconstructor 
{
	public static void Reconstruct(List<Instruction> list)
	{
		for (int index=list.size()-1; index>=0; --index)
		{
			if (list.get(index).opcode != ByteCodeConstants.XRETURN)
				continue;
			
			ReturnInstruction ri1 = (ReturnInstruction)list.get(index);
			int opcode = ri1.valueref.opcode;
			
			if ((opcode != ByteCodeConstants.SIPUSH) && 
				(opcode != ByteCodeConstants.BIPUSH) && 
				(opcode != ByteCodeConstants.ICONST))
				continue;
			
			IConst iConst1 = (IConst)ri1.valueref;

			if (!"Z".equals(iConst1.signature))
				continue;
			
			if (index <= 0)
				continue;
			
			int index2 = index - 1;
						
			if (list.get(index2).opcode != ByteCodeConstants.XRETURN)
				continue;
			
			ReturnInstruction ri2 = (ReturnInstruction)list.get(index2);
			opcode = ri2.valueref.opcode;
			
			if ((opcode != ByteCodeConstants.SIPUSH) && 
				(opcode != ByteCodeConstants.BIPUSH) && 
				(opcode != ByteCodeConstants.ICONST))
				continue;
				
			IConst iConst2 = (IConst)ri2.valueref;

			if (!"Z".equals(iConst2.signature))
				continue;
			
			if (index2 <= 0)
				continue;
			
			Instruction instruction = list.get(--index2);
			opcode = instruction.opcode;
			
			if ((opcode != ByteCodeConstants.IF) && 
				(opcode != ByteCodeConstants.IFCMP) &&
				(opcode != ByteCodeConstants.IFXNULL) &&
				(opcode != ByteCodeConstants.COMPLEXIF))
				continue;
			
			BranchInstruction bi = (BranchInstruction)instruction;			
			int offset = bi.GetJumpOffset();
			
			if ((ri2.offset >= offset) || (offset > ri1.offset))
				continue;
			
			// Verification qu'aucune instruction saute sur 'ri1'
			boolean found = false;
			int i = index2;
			
			while (i-- > 0)
			{
				instruction = list.get(i);
				opcode = instruction.opcode;
				
				if (opcode == ByteCodeConstants.GOTO)
				{
					int jumpOffset = ((Goto)instruction).GetJumpOffset();
					if ((ri2.offset < jumpOffset) && (jumpOffset <= ri1.offset))
					{
						found = true;
						break;
					}
				}
				else if ((opcode == ByteCodeConstants.IF) ||
						 (opcode == ByteCodeConstants.IFCMP) ||
						 (opcode == ByteCodeConstants.IFXNULL) ||
						 (opcode == ByteCodeConstants.COMPLEXIF))
				{
					int jumpOffset = 
						((BranchInstruction)instruction).GetJumpOffset();
					if ((ri2.offset < jumpOffset) && (jumpOffset <= ri1.offset))
					{
						found = true;
						break;
					}										
				}
			}
			
			if (found == true)
				continue;
			
			if (iConst2.value == 1)
				ComparisonInstructionAnalyzer.InverseComparison( bi );
			
			list.remove(index);
			list.remove(index2);
			
			ri2.valueref = bi;
			
			index -= 3;
		}
	}
}
