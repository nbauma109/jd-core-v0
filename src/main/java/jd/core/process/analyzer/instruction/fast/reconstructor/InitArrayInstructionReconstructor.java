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
import java.util.List;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BIPush;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.NewArray;
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

        if (lastAsi != null)
        {
            // Instantiation d'une instruction InitArrayInstruction
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
            if (iai.getNewArray().getOpcode() == Const.NEWARRAY)
            {
                NewArray na = (NewArray)iai.getNewArray();
                switch (na.getType())
                {
                case Const.T_BOOLEAN:
                    setConstantTypes("Z", iai.getValues());
                    break;
                case Const.T_CHAR:
                    setConstantTypes("C", iai.getValues());
                    break;
                case Const.T_BYTE:
                    setConstantTypes("B", iai.getValues());
                    break;
                case Const.T_SHORT:
                    setConstantTypes("S", iai.getValues());
                    break;
                case Const.T_INT:
                    setConstantTypes("I", iai.getValues());
                    break;
                }
            }
        }
    }

    private static void setConstantTypes(
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
