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
package jd.core.process.analyzer.instruction.fast.visitor;

import org.apache.bcel.Const;

import java.util.List;
import java.util.function.Predicate;

import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastFor;
import jd.core.model.instruction.fast.instruction.FastForEach;
import jd.core.model.instruction.fast.instruction.FastInstruction;
import jd.core.model.instruction.fast.instruction.FastLabel;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.instruction.fast.instruction.FastTry.FastCatch;

public final class CheckLocalVariableUsedVisitor
{
    private CheckLocalVariableUsedVisitor() {
    }

    public static boolean visit(Predicate<IndexInstruction> predicate, Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            return visit(predicate, ((ArrayLength)instruction).getArrayref());
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                return visit(predicate, asi.getIndexref()) || visit(predicate, asi.getValueref());
            }
        case  ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                return visit(predicate, ai.getTest()) || ai.getMsg() != null && visit(predicate, ai.getMsg());
            }
        case Const.ATHROW:
            return visit(predicate, ((AThrow)instruction).getValue());
        case ByteCodeConstants.UNARYOP:
            return visit(predicate, ((UnaryOperatorInstruction)instruction).getValue());
        case ByteCodeConstants.BINARYOP:
            {
                BinaryOperatorInstruction boi =
                    (BinaryOperatorInstruction)instruction;
                return visit(predicate, boi.getValue1()) || visit(predicate, boi.getValue2());
            }
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.GETFIELD,
             Const.INSTANCEOF,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.POP:
            return visit(predicate, ((ObjectrefAttribute)instruction).getObjectref());
        case ByteCodeConstants.LOAD,
             Const.ALOAD,
             Const.ILOAD:
            {
                LoadInstruction li = (LoadInstruction)instruction;
                return predicate.test(li);
            }
        case ByteCodeConstants.STORE,
             Const.ASTORE,
             Const.ISTORE:
            {
                StoreInstruction si = (StoreInstruction)instruction;
                return predicate.test(si) || visit(predicate, si.getValueref());
            }
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            return visit(predicate,
                ((ConvertInstruction)instruction).getValue());
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                return visit(predicate, ifCmp.getValue1()) || visit(predicate, ifCmp.getValue2());
            }
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            return visit(predicate, ((IfInstruction)instruction).getValue());
        case ByteCodeConstants.COMPLEXIF:
            {
                List<Instruction> branchList =
                    ((ComplexConditionalBranchInstruction)instruction).getInstructions();
                for (int i=branchList.size()-1; i>=0; --i)
                {
                    if (visit(predicate, branchList.get(i))) {
                        return true;
                    }
                }
                return false;
            }
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            if (visit(predicate, ((InvokeNoStaticInstruction)instruction).getObjectref())) {
                return true;
            }
            // intended fall through
        case Const.INVOKESTATIC:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                for (int i=list.size()-1; i>=0; --i)
                {
                    if (visit(predicate, list.get(i))) {
                        return true;
                    }
                }
                return false;
            }
        case ByteCodeConstants.INVOKENEW:
            {
                List<Instruction> list = ((InvokeNew)instruction).getArgs();
                for (int i=list.size()-1; i>=0; --i)
                {
                    if (visit(predicate, list.get(i))) {
                        return true;
                    }
                }
                return false;
            }
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            return visit(predicate, ((Switch)instruction).getKey());
        case Const.MULTIANEWARRAY:
            {
                Instruction[] dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.length-1; i>=0; --i)
                {
                    if (visit(predicate, dimensions[i])) {
                        return true;
                    }
                }
                return false;
            }
        case Const.NEWARRAY:
            return visit(predicate,
                ((NewArray)instruction).getDimension());
        case Const.ANEWARRAY:
            return visit(predicate,
                ((ANewArray)instruction).getDimension());
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                return visit(predicate, putField.getObjectref()) || visit(predicate, putField.getValueref());
            }
        case ByteCodeConstants.XRETURN,
             Const.PUTSTATIC:
             {
                return visit(predicate, ((ValuerefAttribute)instruction).getValueref());
             }
        case ByteCodeConstants.TERNARYOP:
            {
                TernaryOperator to = (TernaryOperator)instruction;
                return visit(predicate, to.getValue1()) || visit(predicate, to.getValue2());
            }
        case ByteCodeConstants.ASSIGNMENT:
            {
                AssignmentInstruction ai = (AssignmentInstruction)instruction;
                return visit(predicate, ai.getValue1()) || visit(predicate, ai.getValue2());
            }
        case ByteCodeConstants.ARRAYLOAD:
            {
                ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
                return visit(predicate, ali.getArrayref()) || visit(predicate, ali.getIndexref());
            }
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            return visit(predicate,
                ((IncInstruction)instruction).getValue());
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                return visit(predicate, iai.getNewArray()) || iai.getValues() != null && visit(predicate, iai.getValues());
            }
        case FastConstants.FOR:
            {
                FastFor ff = (FastFor)instruction;
                return ff.getInit() != null && visit(predicate, ff.getInit()) || ff.getInc() != null && visit(predicate, ff.getInc())
                    || ff.getInstructions() != null && visit(predicate, ff.getInstructions());
            }
        case FastConstants.WHILE,
             FastConstants.DO_WHILE,
             FastConstants.IF_SIMPLE:
            {
                Instruction test = ((FastTestList)instruction).getTest();
                List<Instruction> instructions = ((FastTestList)instruction).getInstructions();
                return test != null && visit(predicate, test)
                    || instructions != null && visit(predicate, instructions);
            }
        case FastConstants.INFINITE_LOOP:
            {
                List<Instruction> instructions =
                        ((FastList)instruction).getInstructions();
                return instructions != null && visit(predicate, instructions);
            }
        case FastConstants.FOREACH:
            {
                FastForEach ffe = (FastForEach)instruction;
                return visit(predicate, ffe.getVariable()) || visit(predicate, ffe.getValues()) || visit(predicate, ffe.getInstructions());
            }
        case FastConstants.IF_ELSE:
            {
                FastTest2Lists ft2l = (FastTest2Lists)instruction;
                return visit(predicate, ft2l.getTest()) || visit(predicate, ft2l.getInstructions()) || visit(predicate, ft2l.getInstructions2());
            }
        case FastConstants.IF_CONTINUE,
             FastConstants.IF_BREAK,
             FastConstants.IF_LABELED_BREAK,
             FastConstants.GOTO_CONTINUE,
             FastConstants.GOTO_BREAK,
             FastConstants.GOTO_LABELED_BREAK:
            {
                FastInstruction fi = (FastInstruction)instruction;
                return fi.getInstruction() != null && visit(predicate, fi.getInstruction());
            }
        case FastConstants.SWITCH,
             FastConstants.SWITCH_ENUM,
             FastConstants.SWITCH_STRING:
            {
                FastSwitch fs = (FastSwitch)instruction;
                if (visit(predicate, fs.getTest())) {
                    return true;
                }
                FastSwitch.Pair[] pairs = fs.getPairs();
                List<Instruction> instructions;
                for (int i=pairs.length-1; i>=0; --i)
                {
                    instructions = pairs[i].getInstructions();
                    if (instructions != null && visit(predicate, instructions)) {
                        return true;
                    }
                }
                return false;
            }
        case FastConstants.TRY:
            {
                FastTry ft = (FastTry)instruction;
                if (visit(predicate, ft.getInstructions()) || ft.getFinallyInstructions() != null && visit(predicate, ft.getFinallyInstructions())) {
                    return true;
                }
                List<FastCatch> catchs = ft.getCatches();
                for (int i=catchs.size()-1; i>=0; --i) {
                    if (visit(predicate, catchs.get(i).instructions())) {
                        return true;
                    }
                }
                return false;
            }
        case FastConstants.SYNCHRONIZED:
            {
                FastSynchronized fsd = (FastSynchronized)instruction;
                return visit(predicate, fsd.getMonitor()) || visit(predicate, fsd.getInstructions());
            }
        case FastConstants.LABEL:
            {
                FastLabel fl = (FastLabel)instruction;
                return fl.getInstruction() != null && visit(predicate, fl.getInstruction());
            }
        case FastConstants.DECLARE:
            {
                FastDeclaration fd = (FastDeclaration)instruction;
                return fd.getInstruction() != null && visit(predicate, fd.getInstruction());
            }
        case Const.GETSTATIC,
             ByteCodeConstants.OUTERTHIS,
             Const.ACONST_NULL,
             Const.BIPUSH,
             ByteCodeConstants.ICONST,
             ByteCodeConstants.LCONST,
             ByteCodeConstants.FCONST,
             ByteCodeConstants.DCONST,
             Const.GOTO,
             Const.IINC,
             Const.INVOKEDYNAMIC,
             Const.JSR,
             Const.LDC,
             Const.LDC2_W,
             Const.NEW,
             Const.NOP,
             Const.SIPUSH,
             Const.RET,
             Const.RETURN,
             ByteCodeConstants.EXCEPTIONLOAD,
             ByteCodeConstants.RETURNADDRESSLOAD,
             ByteCodeConstants.DUPLOAD:
            return false;
        default:
            System.err.println(
                    "Can not find local variable used in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
            return false;
        }
    }

    public static boolean visit(Predicate<IndexInstruction> predicate,
        List<Instruction> instructions)
    {
        for (int i=instructions.size()-1; i>=0; --i) {
            if (visit(predicate, instructions.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean visit(LocalVariables localVariables, int maxOffset, List<Instruction> instructions)
    {
        return visit(ii -> {
            LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(ii.getIndex(), ii.getOffset());
            return lv != null && maxOffset <= lv.getStartPc();
        }, instructions);
    }

    public static boolean visit(LocalVariable lv, List<Instruction> instructions)
    {
        return visit(new LocalVariables(lv), lv.getStartPc(), instructions);
    }

    public static boolean checkNameUsed(LocalVariables localVariables, int lvNameIndex, List<Instruction> instructions)
    {
        return visit(ii -> {
            LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(ii.getIndex(), ii.getOffset());
            return lv != null && lv.getNameIndex() == lvNameIndex;
        }, instructions);
    }

    public static boolean visit(LocalVariables localVariables, int maxOffset, Instruction instruction)
    {
        return visit(ii -> {
            LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(ii.getIndex(), ii.getOffset());
            return lv != null && maxOffset <= lv.getStartPc();
        }, instruction);
    }
}
