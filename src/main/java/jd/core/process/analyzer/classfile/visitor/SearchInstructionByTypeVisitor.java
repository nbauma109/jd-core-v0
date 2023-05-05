/*******************************************************************************
 * Copyright (C) 2007-2023 Emmanuel Dupuy GPLv3 and other contributors
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
package jd.core.process.analyzer.classfile.visitor;

import org.apache.bcel.Const;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastFor;
import jd.core.model.instruction.fast.instruction.FastForEach;
import jd.core.model.instruction.fast.instruction.FastInstruction;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSwitch.Pair;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.instruction.fast.instruction.FastTry.FastCatch;

public final class SearchInstructionByTypeVisitor<T extends Instruction>
{
    private final Class<T> type;
    private final Predicate<T> predicate;

    public SearchInstructionByTypeVisitor(Class<T> type, Predicate<T> predicate) {
        this.type = type;
        this.predicate = predicate;
    }

    public SearchInstructionByTypeVisitor(Class<T> type) {
        this(type, null);
    }

    public T visit(Instruction instruction)
        throws RuntimeException
    {
        Objects.requireNonNull(instruction, "Null instruction");

        if (type.isInstance(instruction)) {
            T convertedInstruction = type.cast(instruction);
            if (predicate == null || predicate.test(convertedInstruction)) {
                return convertedInstruction;
            }
        }

        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            return visit(((ArrayLength)instruction).getArrayref());
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            return Optional.ofNullable(visit(((ArrayStoreInstruction)instruction).getArrayref()))
                      .orElseGet(() -> visit(((ArrayStoreInstruction)instruction).getValueref()));
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                T tmp = visit(ai.getTest());
                if (tmp != null) {
                    return tmp;
                }
                if (ai.getMsg() == null) {
                    return null;
                }
                return visit(ai.getMsg());
            }
        case Const.ATHROW:
            return visit(((AThrow)instruction).getValue());
        case ByteCodeConstants.UNARYOP:
            return visit(((UnaryOperatorInstruction)instruction).getValue());
        case ByteCodeConstants.BINARYOP,
             ByteCodeConstants.ASSIGNMENT:
            {
                BinaryOperatorInstruction boi =
                    (BinaryOperatorInstruction)instruction;
                return Optional.ofNullable(visit(boi.getValue1())).orElseGet(
                                     () -> visit(boi.getValue2()));
            }
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.GETFIELD,
             Const.INSTANCEOF,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.POP:
            return visit(((ObjectrefAttribute)instruction).getObjectref());
        case ByteCodeConstants.STORE,
             ByteCodeConstants.XRETURN,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTSTATIC:
            return visit(((ValuerefAttribute)instruction).getValueref());
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            return visit(((ConvertInstruction)instruction).getValue());
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                return Optional.ofNullable(visit(ifCmp.getValue1())).orElseGet(
                                     () -> visit(ifCmp.getValue2()));
            }
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            return visit(((IfInstruction)instruction).getValue());
        case ByteCodeConstants.COMPLEXIF:
            {
                List<Instruction> branchList =
                    ((ComplexConditionalBranchInstruction)instruction).getInstructions();
                for (int i=branchList.size()-1; i>=0; --i)
                {
                    T tmp = visit(branchList.get(i));
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            {
                T result = visit(
                    ((InvokeNoStaticInstruction)instruction).getObjectref());
                if (result != null) {
                    return result;
                }
            }
            // intended fall through
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                for (int i=list.size()-1; i>=0; --i)
                {
                    T tmp = visit(list.get(i));
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                Instruction[] dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.length-1; i>=0; --i)
                {
                    T tmp = visit(dimensions[i]);
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
            break;
        case Const.NEWARRAY:
            return visit(((NewArray)instruction).getDimension());
        case Const.ANEWARRAY:
            return visit(((ANewArray)instruction).getDimension());
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                return Optional.ofNullable(visit(putField.getObjectref())).orElseGet(
                                     () -> visit(putField.getValueref()));
            }
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            return visit(((IncInstruction)instruction).getValue());
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                T tmp = visit(iai.getNewArray());
                if (tmp != null) {
                    return tmp;
                }
                if (iai.getValues() != null) {
                    return visit(iai.getValues());
                }
            }
            break;
        case ByteCodeConstants.TERNARYOP:
            {
                TernaryOperator to = (TernaryOperator)instruction;
                return Optional.ofNullable(visit(to.getValue1())).orElseGet(
                                     () -> visit(to.getValue2()));
            }
        case FastConstants.TRY:
            {
                FastTry ft = (FastTry)instruction;
                T tmp = visit(ft.getInstructions());
                if (tmp != null) {
                    return tmp;
                }
                List<FastCatch> catches = ft.getCatches();
                for (int i=catches.size()-1; i>=0; --i)
                {
                    tmp = visit(catches.get(i).instructions());
                    if (tmp != null) {
                        return tmp;
                    }
                }
                if (ft.getFinallyInstructions() != null) {
                    return visit(ft.getFinallyInstructions());
                }
            }
            break;
        case FastConstants.SYNCHRONIZED:
            {
                FastSynchronized fsy = (FastSynchronized)instruction;
                return Optional.ofNullable(visit(fsy.getMonitor())).orElseGet(
                                     () -> visit(fsy.getInstructions()));
            }
        case FastConstants.FOR:
            {
                FastFor ff = (FastFor)instruction;
                if (ff.getInit() != null)
                {
                    T tmp = visit(ff.getInit());
                    if (tmp != null) {
                        return tmp;
                    }
                }
                if (ff.getInc() != null)
                {
                    T tmp = visit(ff.getInc());
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
            // intended fall through
        case FastConstants.WHILE,
             FastConstants.DO_WHILE,
             FastConstants.IF_SIMPLE:
            {
                FastTestList ftl = (FastTestList)instruction;
                if (ftl.getTest() != null)
                {
                    T tmp = visit(ftl.getTest());
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
            // intended fall through
        case FastConstants.INFINITE_LOOP:
            {
                List<Instruction> instructions =
                        ((FastList)instruction).getInstructions();
                if (instructions != null) {
                    return visit(instructions);
                }
            }
            break;
        case FastConstants.FOREACH:
            {
                FastForEach ffe = (FastForEach)instruction;
                T tmp = visit(ffe.getVariable());
                if (tmp != null) {
                    return tmp;
                }
                tmp = visit(ffe.getValues());
                if (tmp != null) {
                    return tmp;
                }
                return visit(ffe.getInstructions());
            }
        case FastConstants.IF_ELSE:
            {
                FastTest2Lists ft2l = (FastTest2Lists)instruction;
                T tmp = visit(ft2l.getTest());
                if (tmp != null) {
                    return tmp;
                }
                tmp = visit(ft2l.getInstructions());
                if (tmp != null) {
                    return tmp;
                }
                return visit(ft2l.getInstructions2());
            }
        case FastConstants.IF_CONTINUE,
             FastConstants.IF_BREAK,
             FastConstants.IF_LABELED_BREAK,
             FastConstants.GOTO_CONTINUE,
             FastConstants.GOTO_BREAK,
             FastConstants.GOTO_LABELED_BREAK:
            {
                FastInstruction fi = (FastInstruction)instruction;
                if (fi.getInstruction() != null) {
                    return visit(fi.getInstruction());
                }
            }
            break;
        case FastConstants.DECLARE:
            {
                FastDeclaration fd = (FastDeclaration)instruction;
                if (fd.getInstruction() != null) {
                    return visit(fd.getInstruction());
                }
            }
            break;
        case FastConstants.SWITCH,
             FastConstants.SWITCH_ENUM,
             FastConstants.SWITCH_STRING:
            {
                FastSwitch fs = (FastSwitch)instruction;
                T tmp = visit(fs.getTest());
                if (tmp != null) {
                    return tmp;
                }

                Pair[] pairs = fs.getPairs();
                for (int i=pairs.length-1; i>=0; --i)
                {
                    List<Instruction> instructions = pairs[i].getInstructions();
                    if (instructions != null)
                    {
                        tmp = visit(instructions);
                        if (tmp != null) {
                            return tmp;
                        }
                    }
                }
            }
            break;
        case Const.ACONST_NULL,
             ByteCodeConstants.ARRAYLOAD,
             ByteCodeConstants.LOAD,
             Const.ALOAD,
             Const.ILOAD,
             Const.BIPUSH,
             ByteCodeConstants.ICONST,
             ByteCodeConstants.LCONST,
             ByteCodeConstants.FCONST,
             ByteCodeConstants.DCONST,
             ByteCodeConstants.DUPLOAD,
             ByteCodeConstants.EXCEPTIONLOAD,
             Const.GETSTATIC,
             ByteCodeConstants.OUTERTHIS,
             Const.GOTO,
             Const.IINC,
             Const.JSR,
             Const.LDC,
             Const.LDC2_W,
             Const.NEW,
             Const.NOP,
             Const.RET,
             Const.RETURN,
             Const.INVOKEDYNAMIC,
             ByteCodeConstants.RETURNADDRESSLOAD,
             Const.SIPUSH:
            break;
        default:
            System.err.println(
                    "Can not search instruction in " +
                    instruction.getClass().getName() +
                    "=" + instruction.getOpcode());
        }

        return null;
    }

    private T visit(List<Instruction> instructions)
    {
        for (int i=instructions.size()-1; i>=0; --i)
        {
            T instruction = visit(instructions.get(i));
            if (instruction != null) {
                return instruction;
            }
        }

        return null;
    }
}
