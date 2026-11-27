/**
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
 */
package jd.core.process.analyzer.instruction.fast.reconstructor;

import org.apache.bcel.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BIPush;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.SIPush;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.process.analyzer.util.ReconstructorUtil;

/**
 * Reconstruction des initialisation de tableaux depuis le motif :
 * DupStore0 ( NewArray | ANewArray ... )
 * ?AStore ( DupLoad0, index=0, value )
 * DupStore1 ( DupLoad0 )
 * ?AStore ( DupLoad1, index=1, value )
 * DupStore2 ( DupLoad1 )
 * ?AStore ( DupLoad2, index=2, value )
 * ...
 * ???( DupLoadN )
 *
 * Cette operation doit être executee avant 'AssignmentInstructionReconstructor'.
 */
public final class InitArrayInstructionReconstructor
{
    private InitArrayInstructionReconstructor() {
    }

    public static void reconstruct(List<Instruction> list)
    {
        Instruction i;
        DupStore dupStore;
        int opcode;
        for (int index=list.size()-1; index>=0; --index)
        {
            i = list.get(index);

            if (i.getOpcode() != ByteCodeConstants.DUPSTORE) {
                continue;
            }

            dupStore = (DupStore)i;
            opcode = dupStore.getObjectref().getOpcode();

            if (opcode != Const.NEWARRAY &&
                    opcode != Const.ANEWARRAY) {
                continue;
            }

            reconstructAInstruction(list, index, dupStore);
        }
    }

    /** @return true if a switch at the index precedes the next element stored in the array */
    private static boolean isInterruptedBySwitch(List<Instruction> list, int index, DupStore lastDupStore)
    {
        return index < list.size() && list.get(index) instanceof Switch
            && list.subList(index, list.size()).stream().anyMatch(instruction ->
                instruction instanceof ArrayStoreInstruction asi
                    && isDuplicate(asi.getArrayref(), Set.of(lastDupStore.getOffset())));
    }

    private static void reconstructAInstruction(
            List<Instruction> list, int index, DupStore dupStore)
    {
        // 1er DupStore trouvé
        final int length = list.size();
        int firstDupStoreIndex = index;
        DupStore lastDupStore = dupStore;
        ArrayStoreInstruction lastAsi = null;
        int arrayIndex = 0;
        List<Instruction> values = new ArrayList<>();

        Instruction i;
        int indexOfArrayStoreInstruction;
        DupStore nextDupStore;
        while (++index < length)
        {
            i = list.get(index);

            // Recherche de ?AStore ( DupLoad, index, value )
            if (i.getOpcode() != Const.AASTORE &&
                    i.getOpcode() != ByteCodeConstants.ARRAYSTORE) {
                break;
            }

            ArrayStoreInstruction asi = (ArrayStoreInstruction)i;

            if (asi.getArrayref().getOpcode() != ByteCodeConstants.DUPLOAD ||
                    asi.getArrayref().getOffset() != lastDupStore.getOffset()) {
                break;
            }

            lastAsi = asi;

            // Si les premieres cases d'un tableau ont pour valeur 0, elles
            // ne sont pas initialisees ! La boucle suivante reconstruit
            // l'initialisation des valeurs 0.
            indexOfArrayStoreInstruction = getArrayIndex(asi.getIndexref());
            while (indexOfArrayStoreInstruction > arrayIndex)
            {
                values.add(new IConst(
                        ByteCodeConstants.ICONST, asi.getOffset(), asi.getLineNumber(), 0));
                arrayIndex++;
            }

            values.add(asi.getValueref());
            arrayIndex++;

            index++;
            // Recherche de DupStoreM( DupLoadN )
            if (index >= length) {
                break;
            }

            i = list.get(index);

            if (i.getOpcode() != ByteCodeConstants.DUPSTORE) {
                break;
            }

            nextDupStore = (DupStore)i;

            if (nextDupStore.getObjectref().getOpcode() != ByteCodeConstants.DUPLOAD ||
                    nextDupStore.getObjectref().getOffset() != lastDupStore.getOffset()) {
                break;
            }

            lastDupStore = nextDupStore;
        }

        if (isInterruptedBySwitch(list, index, lastDupStore))
        {
            // The value of an element computed by a switch expression follows the switch in the instructions: the array
            // cannot be initialized with only the elements preceding it, which are stored one by one
            setElementTypes(dupStore.getObjectref(), getElementValues(list, firstDupStoreIndex));
        }
        else if (lastAsi != null)
        {
            // Instanciation d'une instruction InitArrayInstruction
            InitArrayInstruction iai = new InitArrayInstruction(
                    ByteCodeConstants.NEWANDINITARRAY, lastAsi.getOffset(),
                    dupStore.getLineNumber(), dupStore.getObjectref(), values);

            // Recherche de l'instruction 'DupLoad' suivante
            Instruction parent = ReconstructorUtil.replaceDupLoad(
                    list, index, lastDupStore, iai);

            if (parent != null && parent.getOpcode() == ByteCodeConstants.TERNARYOP) {
                TernaryOperator fto = (TernaryOperator) parent;
                if (fto.getValue1() == iai && fto.getValue2().getOpcode() == Const.AASTORE) {
                    ArrayStoreInstruction asi = (ArrayStoreInstruction) fto.getValue2();
                    if (asi.getArrayref().getOpcode() == ByteCodeConstants.DUPLOAD) {
                        DupLoad dl = (DupLoad) asi.getArrayref();
                        if (dl.getDupStore() == list.get(index)) {
                            fto.setValue2(new InitArrayInstruction(
                                ByteCodeConstants.NEWANDINITARRAY, asi.getOffset(),
                                dl.getDupStore().getLineNumber(), dl.getDupStore().getObjectref(),
                                Collections.singletonList(asi.getValueref())));
                            ReconstructorUtil.replaceDupLoad(list, index, dl.getDupStore(), fto);
                            list.remove(fto);
                        }
                    }
                }
            }
            if (parent != null && parent.getOpcode() == Const.AASTORE) {
                if (list.get(index).getOpcode() == ByteCodeConstants.TERNARYOP) {
                    TernaryOperator fto = (TernaryOperator) list.get(index);
                    if (fto.getValue2().getOpcode() == Const.AASTORE) {
                        ArrayStoreInstruction asi = (ArrayStoreInstruction) fto.getValue2();
                        if (asi.getArrayref() == iai && asi.getIndexref().getOpcode() == ByteCodeConstants.ICONST) {
                            IConst iConst = (IConst) asi.getIndexref();
                            values.add(iConst.getValue(), asi.getValueref());
                            fto.setValue2(iai);
                            ReconstructorUtil.replaceDupLoad(list, index, lastDupStore, fto);
                            list.remove(index);
                        }
                    }
                } else {
                    iai.setOpcode(ByteCodeConstants.INITARRAY);
                }
            }
            // Retrait des instructions de la liste
            while (firstDupStoreIndex < index) {
                index--;
                list.remove(index);
            }

            // Initialisation des types de constantes entieres
            setElementTypes(iai.getNewArray(), iai.getValues());
        }
    }

    private static void setElementTypes(Instruction newArray, List<Instruction> values)
    {
        if (newArray instanceof NewArray na)
        {
            switch (na.getType())
            {
            case Const.T_BOOLEAN:
                setContantTypes("Z", values);
                break;
            case Const.T_CHAR:
                setContantTypes("C", values);
                break;
            case Const.T_BYTE:
                setContantTypes("B", values);
                break;
            case Const.T_SHORT:
                setContantTypes("S", values);
                break;
            case Const.T_INT:
                setContantTypes("I", values);
                break;
            }
        }
    }

    /** @return the values stored in the array duplicated at the given index, and in its own duplicates */
    private static List<Instruction> getElementValues(List<Instruction> list, int firstDupStoreIndex)
    {
        Set<Integer> duplicates = new HashSet<>();
        duplicates.add(list.get(firstDupStoreIndex).getOffset());
        List<Instruction> values = new ArrayList<>();

        for (Instruction instruction : list.subList(firstDupStoreIndex + 1, list.size()))
        {
            if (instruction instanceof DupStore store && isDuplicate(store.getObjectref(), duplicates)) {
                duplicates.add(store.getOffset());
            } else if (instruction instanceof ArrayStoreInstruction asi && isDuplicate(asi.getArrayref(), duplicates)) {
                values.add(asi.getValueref());
            }
        }

        return values;
    }

    private static boolean isDuplicate(Instruction instruction, Set<Integer> duplicates)
    {
        return instruction.getOpcode() == ByteCodeConstants.DUPLOAD && duplicates.contains(instruction.getOffset());
    }

    private static void setContantTypes(
            String signature, List<Instruction> values)
    {
        for (Instruction value : values)
        {
            if (value.getOpcode() == Const.BIPUSH
             || value.getOpcode() == ByteCodeConstants.ICONST
             || value.getOpcode() == Const.SIPUSH) {
                ((IConst)value).setReturnedSignature(signature);
            }
        }
    }

    private static int getArrayIndex(Instruction i)
    {
        return switch (i.getOpcode())
        {
        case ByteCodeConstants.ICONST -> ((IConst)i).getValue();
        case Const.BIPUSH             -> ((BIPush)i).getValue();
        case Const.SIPUSH             -> ((SIPush)i).getValue();
        default                       -> -1;
        };
    }
}
