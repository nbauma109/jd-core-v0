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
package jd.core.process.analyzer.instruction.fast.visitor;

import org.apache.bcel.Const;

import java.util.List;

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
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.SwitchExpression;
import jd.core.model.instruction.bytecode.instruction.SwitchExpressionYield;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastFor;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;

public abstract class AbstractReplaceInstructionVisitor<T>
{
    private Instruction oldInstruction;
    private Instruction newInstruction;
    private Instruction parentFound;

    protected AbstractReplaceInstructionVisitor(Instruction newInstruction)
    {
        init(newInstruction);
    }

    protected AbstractReplaceInstructionVisitor()
    {
        this(null);
    }

    protected void init(Instruction newInstruction)
    {
        this.oldInstruction = null;
        this.newInstruction = newInstruction;
        this.parentFound = null;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            {
                ArrayLength al = (ArrayLength)instruction;
                T found = match(al, al.getArrayref());
                if (found != null)
                {
                    al.setArrayref(newInstruction(al.getArrayref(), found));
                }
                visit(al.getArrayref());
            }
            break;
        case ByteCodeConstants.ARRAYLOAD:
            {
                ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
                T found = match(ali, ali.getArrayref());
                if (found != null)
                {
                    ali.setArrayref(newInstruction(ali.getArrayref(), found));
                }
                visit(ali.getArrayref());

                if (this.oldInstruction == null)
                {
                    found = match(ali, ali.getIndexref());
                    if (found != null) {
                        ali.setIndexref(newInstruction(ali.getIndexref(), found));
                    }
                    visit(ali.getIndexref());
                }
            }
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                T found = match(asi, asi.getArrayref());
                if (found != null)
                {
                    asi.setArrayref(newInstruction(asi.getArrayref(), found));
                }
                visit(asi.getArrayref());

                if (this.oldInstruction == null)
                {
                    found = match(asi, asi.getIndexref());
                    if (found != null)
                    {
                        asi.setIndexref(newInstruction(asi.getIndexref(), found));
                    }
                    visit(asi.getIndexref());

                    if (this.oldInstruction == null)
                    {
                        found = match(asi, asi.getValueref());
                        if (found != null)
                        {
                            asi.setValueref(newInstruction(asi.getValueref(), found));
                        }
                        visit(asi.getValueref());
                    }
                }
            }
            break;
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                T found = match(ai, ai.getTest());
                if (found != null)
                {
                    ai.setTest(newInstruction(ai.getTest(), found));
                }
                visit(ai.getTest());

                if (this.oldInstruction == null && ai.getMsg() != null)
                {
                    found = match(ai, ai.getMsg());
                    if (found != null)
                    {
                        ai.setMsg(newInstruction(ai.getMsg(), found));
                    }
                    visit(ai.getMsg());
                }
            }
            break;
        case Const.ATHROW:
            {
                AThrow aThrow = (AThrow)instruction;
                T found = match(aThrow, aThrow.getValue());
                if (found != null)
                {
                    aThrow.setValue(newInstruction(aThrow.getValue(), found));
                }
                visit(aThrow.getValue());
            }
            break;
        case ByteCodeConstants.UNARYOP:
            {
                UnaryOperatorInstruction uoi = (UnaryOperatorInstruction)instruction;
                T found = match(uoi, uoi.getValue());
                if (found != null)
                {
                    uoi.setValue(newInstruction(uoi.getValue(), found));
                }
                visit(uoi.getValue());
            }
            break;
        case ByteCodeConstants.BINARYOP:
            {
                BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
                T found = match(boi, boi.getValue1());
                if (found != null)
                {
                    boi.setValue1(newInstruction(boi.getValue1(), found));
                }
                visit(boi.getValue1());

                if (this.oldInstruction == null)
                {
                    found = match(boi, boi.getValue2());
                    if (found != null)
                    {
                        boi.setValue2(newInstruction(boi.getValue2(), found));
                    }
                    visit(boi.getValue2());
                }
            }
            break;
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.GETFIELD,
             Const.INSTANCEOF,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.POP:
            {
                visitObjectref(instruction);
            }
            break;
        case ByteCodeConstants.XRETURN,
             ByteCodeConstants.STORE,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTSTATIC:
            {
                visitValueref(instruction);
            }
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            {
                ConvertInstruction ci = (ConvertInstruction)instruction;
                T found = match(ci, ci.getValue());
                if (found != null)
                {
                    ci.setValue(newInstruction(ci.getValue(), found));
                }
                visit(ci.getValue());
            }
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                T found = match(ifCmp, ifCmp.getValue1());
                if (found != null)
                {
                    ifCmp.setValue1(newInstruction(ifCmp.getValue1(), found));
                }
                visit(ifCmp.getValue1());

                if (this.oldInstruction == null)
                {
                    found = match(ifCmp, ifCmp.getValue2());
                    if (found != null)
                    {
                        ifCmp.setValue2(newInstruction(ifCmp.getValue2(), found));
                    }
                    visit(ifCmp.getValue2());
                }
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            {
                IfInstruction iff = (IfInstruction)instruction;
                T found = match(iff, iff.getValue());
                if (found != null)
                {
                    iff.setValue(newInstruction(iff.getValue(), found));
                }
                visit(iff.getValue());
            }
            break;
        case ByteCodeConstants.COMPLEXIF:
            {
                visit(instruction, ((ComplexConditionalBranchInstruction)instruction).getInstructions());
            }
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            {
                visitObjectref(instruction);
            }
            // intended fall through
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            {
                visit(instruction, ((InvokeInstruction)instruction).getArgs());
            }
            break;
        case Const.LOOKUPSWITCH,
             Const.TABLESWITCH:
            {
                Switch ls = (Switch)instruction;
                T found = match(ls, ls.getKey());
                if (found != null)
                {
                    ls.setKey(newInstruction(ls.getKey(), found));
                }
                visit(ls.getKey());
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                visit(instruction, ((MultiANewArray)instruction).getDimensions());
            }
            break;
        case Const.NEWARRAY:
            {
                NewArray newArray = (NewArray)instruction;
                T found = match(newArray, newArray.getDimension());
                if (found != null)
                {
                    newArray.setDimension(newInstruction(newArray.getDimension(), found));
                }
                visit(newArray.getDimension());
            }
            break;
        case Const.ANEWARRAY:
            {
                ANewArray aNewArray = (ANewArray)instruction;
                T found = match(aNewArray, aNewArray.getDimension());
                if (found != null)
                {
                    aNewArray.setDimension(newInstruction(aNewArray.getDimension(), found));
                }
                visit(aNewArray.getDimension());
            }
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                T found = match(putField, putField.getObjectref());
                if (found != null)
                {
                    putField.setObjectref(newInstruction(putField.getObjectref(), found));
                }
                visit(putField.getObjectref());

                if (this.oldInstruction == null)
                {
                    visitValueref(putField);
                }
            }
            break;
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            {
                IncInstruction ii = (IncInstruction)instruction;
                T found = match(ii, ii.getValue());
                if (found != null)
                {
                    ii.setValue(newInstruction(ii.getValue(), found));
                }
                visit(ii.getValue());
            }
            break;
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                T found = match(iai, iai.getNewArray());
                if (found != null)
                {
                    iai.setNewArray(newInstruction(iai.getNewArray(), found));
                }
                visit(iai.getNewArray());

                if (this.oldInstruction == null && iai.getValues() != null) {
                    visit(iai, iai.getValues());
                }
            }
            break;
        case ByteCodeConstants.TERNARYOP:
            {
                TernaryOperator to = (TernaryOperator)instruction;
                T found = match(to, to.getTest());
                if (found != null)
                {
                    to.setTest(newInstruction(to.getTest(), found));
                }
                visit(to.getTest());

                if (this.oldInstruction == null)
                {
                    found = match(to, to.getValue1());
                    if (found != null)
                    {
                        to.setValue1(newInstruction(to.getValue1(), found));
                    }
                    visit(to.getValue1());

                    if (this.oldInstruction == null)
                    {
                        found = match(to, to.getValue2());
                        if (found != null)
                        {
                            to.setValue2(newInstruction(to.getValue2(), found));
                        }
                        visit(to.getValue2());
                    }
                }
            }
            break;
        case ByteCodeConstants.ASSIGNMENT:
            {
                AssignmentInstruction ai = (AssignmentInstruction)instruction;
                T found = match(ai, ai.getValue1());
                if (found != null)
                {
                    ai.setValue1(newInstruction(ai.getValue1(), found));
                }
                visit(ai.getValue1());

                if (this.oldInstruction == null)
                {
                    found = match(ai, ai.getValue2());
                    if (found != null) {
                        ai.setValue2(newInstruction(ai.getValue2(), found));
                    }
                    visit(ai.getValue2());
                }
            }
            break;
        case FastConstants.FOR:
            {
                FastFor ff = (FastFor)instruction;

                if (ff.getInit() != null)
                {
                    T found = match(ff, ff.getInit());
                    if (found != null) {
                        ff.setInit(newInstruction(ff.getInit(), found));
                    }
                    visit(ff.getInit());
                }

                if (this.oldInstruction == null && ff.getInc() != null)
                {
                    T found = match(ff, ff.getInc());
                    if (found != null) {
                        ff.setInc(newInstruction(ff.getInc(), found));
                    }
                    visit(ff.getInc());
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
                    T found = match(ftl, ftl.getTest());
                    if (found != null) {
                        ftl.setTest(newInstruction(ftl.getTest(), found));
                    }
                    visit(ftl.getTest());
                }
            }
            // intended fall through
        case FastConstants.INFINITE_LOOP:
            {
                List<Instruction> instructions =
                        ((FastList)instruction).getInstructions();
                if (instructions != null) {
                    visit(instruction, instructions);
                }
            }
            break;
        case FastConstants.SWITCH,
             FastConstants.SWITCH_ENUM,
             FastConstants.SWITCH_STRING:
            {
                FastSwitch fs = (FastSwitch)instruction;
                T found = match(fs, fs.getTest());
                if (found != null)
                {
                    fs.setTest(newInstruction(fs.getTest(), found));
                }
                visit(fs.getTest());

                FastSwitch.Pair[] pairs = fs.getPairs();
                for (int i=pairs.length-1; i>=0 && this.oldInstruction == null; --i) {
                    visit(fs, pairs[i].getInstructions());
                }
            }
            break;
        case FastConstants.SWITCH_EXPRESSION:
            {
                SwitchExpression expression = (SwitchExpression)instruction;
                FastSwitch fs = expression.getSwitch();
                T found = match(fs, fs.getTest());
                if (found != null)
                {
                    fs.setTest(newInstruction(fs.getTest(), found));
                }
                visit(fs.getTest());

                FastSwitch.Pair[] pairs = fs.getPairs();
                for (int i=pairs.length-1; i>=0 && this.oldInstruction == null; --i) {
                    visit(fs, pairs[i].getInstructions());
                }
            }
            break;
        case FastConstants.SWITCH_EXPRESSION_YIELD:
            {
                SwitchExpressionYield yield = (SwitchExpressionYield)instruction;
                T found = match(yield, yield.getValue());
                if (found != null)
                {
                    yield.setValue(newInstruction(yield.getValue(), found));
                }
                visit(yield.getValue());
            }
            break;
        case FastConstants.TRY:
            {
                FastTry ft = (FastTry)instruction;
                visit(ft, ft.getResources());
                visit(ft, ft.getInstructions());

                if (this.oldInstruction == null)
                {
                    if (ft.getFinallyInstructions() != null) {
                        visit(ft, ft.getFinallyInstructions());
                    }

                    for (int i=ft.getCatches().size()-1; i>=0 && this.oldInstruction == null; --i) {
                        visit(ft, ft.getCatches().get(i).instructions());
                    }
                }
            }
            break;
        case FastConstants.DECLARE:
            {
                FastDeclaration fd = (FastDeclaration)instruction;

                if (fd.getInstruction() != null)
                {
                    T found = match(fd, fd.getInstruction());
                    if (found != null)
                    {
                        fd.setInstruction(newInstruction(fd.getInstruction(), found));
                    }
                    visit(fd.getInstruction());
                }
            }
            break;
        case FastConstants.SYNCHRONIZED:
            {
                FastSynchronized fsy = (FastSynchronized)instruction;

                T found = match(fsy, fsy.getMonitor());
                if (found != null)
                {
                    fsy.setMonitor(newInstruction(fsy.getMonitor(), found));
                }
                visit(fsy.getMonitor());

                if (this.oldInstruction == null) {
                    visit(fsy, fsy.getInstructions());
                }
            }
            break;
        case Const.ACONST_NULL,
             ByteCodeConstants.LOAD,
             Const.ALOAD,
             Const.ILOAD,
             Const.BIPUSH,
             ByteCodeConstants.ICONST,
             ByteCodeConstants.LCONST,
             ByteCodeConstants.FCONST,
             ByteCodeConstants.DCONST,
             ByteCodeConstants.DUPLOAD,
             Const.GETSTATIC,
             ByteCodeConstants.OUTERTHIS,
             Const.GOTO,
             Const.IINC,
             Const.JSR,
             Const.LDC,
             Const.LDC2_W,
             Const.NEW,
             Const.NOP,
             Const.SIPUSH,
             Const.RET,
             Const.RETURN,
             Const.INVOKEDYNAMIC,
             ByteCodeConstants.EXCEPTIONLOAD,
             ByteCodeConstants.RETURNADDRESSLOAD:
            break;
        default:
            System.err.println(
                    "Can not replace code in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    private void visitValueref(Instruction instruction)
    {
        ValuerefAttribute v = (ValuerefAttribute) instruction;
        T found = match(instruction, v.getValueref());
        if (found != null)
        {
            v.setValueref(newInstruction(v.getValueref(), found));
        }
        visit(v.getValueref());
    }

    private void visitObjectref(Instruction instruction)
    {
        ObjectrefAttribute o = (ObjectrefAttribute) instruction;
        T found = match(instruction, o.getObjectref());
        if (found != null)
        {
            o.setObjectref(newInstruction(o.getObjectref(), found));
        }
        visit(o.getObjectref());
    }

    public void visit(Instruction parent, List<Instruction> instructions)
    {
        for (int i=instructions.size()-1; i>=0 && this.oldInstruction == null; --i)
        {
            T found = match(parent, instructions.get(i));
            if (found != null)
            {
                instructions.set(i, newInstruction(instructions.get(i), found));
            }
            visit(instructions.get(i));
        }
    }

    public void visit(List<Instruction> instructions)
    {
        visit(null, instructions);
    }

    public Instruction getOldInstruction()
    {
        return oldInstruction;
    }

    public void setOldInstruction(Instruction oldInstruction)
    {
        this.oldInstruction = oldInstruction;
    }

    protected T match(Instruction parentFound, Instruction instruction)
    {
        T found = match(instruction);
        if (found != null) {
            this.oldInstruction = instruction;
            this.parentFound = parentFound;
            return found;
        }

        return null;
    }

    protected abstract T match(Instruction instruction);

    @SuppressWarnings("unused")
    protected Instruction newInstruction(Instruction i, T item)
    {
        return this.newInstruction;
    }

    public Instruction getParentFound()
    {
        return parentFound;
    }
}
