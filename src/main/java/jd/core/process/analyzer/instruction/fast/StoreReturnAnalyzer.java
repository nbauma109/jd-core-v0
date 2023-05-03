/*******************************************************************************
 * Copyright (C) 2007-2019 Emmanuel Dupuy GPLv3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jd.core.process.analyzer.instruction.fast;

import org.apache.bcel.Const;

import java.util.List;

import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;


/**
 * Compacte les instructions 'store' suivies d'instruction 'return'.
 */
public class StoreReturnAnalyzer 
{
	public static void cleanup(
		List<Instruction> list, LocalVariables localVariables)
	{
		int index = list.size();

		while (index-- > 1)
		{
			if (list.get(index).getOpcode() != ByteCodeConstants.XRETURN) {
                continue;
            }
			
			ReturnInstruction ri = (ReturnInstruction)list.get(index);
			
			if (ri.getLineNumber() == Instruction.UNKNOWN_LINE_NUMBER) {
                continue;
            }
			
			switch (ri.getValueref().getOpcode())
			{
			case Const.ALOAD:
				if (list.get(index-1).getOpcode() == Const.ASTORE) {
                    index = compact(list, localVariables, ri, index);
                }
				break;
			case ByteCodeConstants.LOAD:
				if (list.get(index-1).getOpcode() == ByteCodeConstants.STORE) {
                    index = compact(list, localVariables, ri, index);
                }
				break;
			case Const.ILOAD:
				if (list.get(index-1).getOpcode() == Const.ISTORE) {
                    index = compact(list, localVariables, ri, index);
                }
				break;
			}
		}
	}
	
	private static int compact(
		List<Instruction> list, LocalVariables localVariables, 
		ReturnInstruction ri, int index)
	{
		IndexInstruction load = (IndexInstruction)ri.getValueref();
		StoreInstruction store = (StoreInstruction)list.get(index-1);
		
		if (load.getIndex() == store.getIndex() && 
			load.getLineNumber() == store.getLineNumber())
		{
			// Remove local variable
			LocalVariable lv = localVariables.
				getLocalVariableWithIndexAndOffset(
						store.getIndex(), store.getOffset());
			
			if (lv != null && lv.getStartPc() == store.getOffset() && 
				lv.getStartPc() + lv.getLength() <= ri.getOffset()) {
                localVariables.
					removeLocalVariableWithIndexAndOffset(
							store.getIndex(), store.getOffset());
            }
			// Replace returned instruction
			ri.setValueref(store.getValueref());
			// Remove 'store' instruction
			list.remove(--index);						
		}	
		
		return index;
	}
}
