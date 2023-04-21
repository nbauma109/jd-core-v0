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
package jd.core.model.instruction.fast.instruction;

import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.layouter.visitor.MinLineNumberVisitor;

/**
 * try-catch-finally
 */
public class FastTry extends FastList {
    private final List<FastCatch> catches;
    private final List<Instruction> finallyInstructions;
    private final List<Instruction> resources;

    public FastTry(int opcode, int offset, int lineNumber, int branch, List<Instruction> instructions, List<FastCatch> catches, List<Instruction> finallyInstructions) {
        super(opcode, offset, lineNumber, branch, instructions);
        this.catches = catches;
        this.finallyInstructions = finallyInstructions;
        this.resources = new ArrayList<>();
    }

    public List<FastCatch> getCatches() {
        return catches;
    }

    public List<Instruction> getFinallyInstructions() {
        return finallyInstructions;
    }

    public static class FastCatch {
        int exceptionOffset;
        int exceptionTypeIndex;
        int[] otherExceptionTypeIndexes;
        int localVarIndex;
        List<Instruction> instructions;

        public FastCatch(int exceptionOffset, int exceptionTypeIndex, int[] otherExceptionTypeIndexes, int localVarIndex, List<Instruction> instructions) {
            this.exceptionOffset = exceptionOffset;
            this.exceptionTypeIndex = exceptionTypeIndex;
            this.otherExceptionTypeIndexes = otherExceptionTypeIndexes;
            this.localVarIndex = localVarIndex;
            this.instructions = instructions;
        }

        public boolean removeOutOfBounds(int firstLineNumber) {
            return instructions.removeIf(instr -> instr.getLineNumber() < firstLineNumber);
        }

        public int minLineNumber() {
            return instructions
                    .stream()
                    .min(Comparator.comparing(MinLineNumberVisitor::visit))
                    .map(Instruction::getLineNumber)
                    .orElse(Integer.MAX_VALUE);
        }

        public int localVarIndex() {
            return localVarIndex;
        }

        public int exceptionOffset() {
            return exceptionOffset;
        }

        public int exceptionTypeIndex() {
            return exceptionTypeIndex;
        }

        public List<Instruction> instructions() {
            return instructions;
        }

        public int[] otherExceptionTypeIndexes() {
            return otherExceptionTypeIndexes;
        }
    }

    public void removeOutOfBounds() {
        if (getInstructions().isEmpty()) {
            return;
        }
        int firstLineNumber = instructions.get(0).getLineNumber();
        for (Instruction instruction : instructions) {
            if (instruction instanceof FastTry) {
                FastTry fastTry = (FastTry) instruction;
                fastTry.removeOutOfBounds();
                if (!fastTry.getInstructions().isEmpty()) {
                    fastTry.cleanUpCatches(fastTry.getInstructions().get(0).getLineNumber());
                }
            }
        }
        cleanUpCatches(firstLineNumber);
    }

    private void cleanUpCatches(int firstLineNumber) {
        for (Iterator<FastCatch> iterator = catches.iterator(); iterator.hasNext();) {
            FastCatch fastCatch = iterator.next();
            if (fastCatch.removeOutOfBounds(firstLineNumber) && fastCatch.instructions().isEmpty()) {
                iterator.remove();
            }
        }
    }

    public boolean removeTryResourcesPattern(LocalVariables localVariables, ConstantPool cp, List<Instruction> instructions) {
        if (instructions == null || instructions.size() < 4) {
            return false;
        }
        for (int i = instructions.size() - 1; i >= 3; i--) {
            Instruction firstInstruction = instructions.get(i-3);
            int firstLineNumber = firstInstruction.getLineNumber();
            if (instructions.subList(i-3, i).stream().anyMatch(instr -> instr.getLineNumber() != firstLineNumber)) {
                continue;
            }
            if (!(firstInstruction instanceof IfInstruction)) {
                continue;
            }
            if (!(instructions.get(i-2) instanceof IfInstruction)) {
                continue;
            }
            if (!(instructions.get(i-1) instanceof FastTry)) {
                continue;
            }
            if (!(instructions.get(i) instanceof InvokeNoStaticInstruction)) {
                continue;
            }
            IfInstruction firstIf = (IfInstruction) firstInstruction;
            IfInstruction secondIf = (IfInstruction) instructions.get(i-2);
            FastTry fastTry = (FastTry) instructions.get(i-1);
            InvokeNoStaticInstruction invokeNoStaticInstruction = (InvokeNoStaticInstruction) instructions.get(i);
            if (!(firstIf.getValue() instanceof ALoad)) {
                continue;
            }
            if (!(invokeNoStaticInstruction.getObjectref() instanceof ALoad)) {
                continue;
            }
            if (((ALoad) firstIf.getValue()).getIndex() != ((ALoad) invokeNoStaticInstruction.getObjectref()).getIndex()) {
                continue;
            }
            if (fastTry.getInstructions() == null || fastTry.getInstructions().size() != 1) {
                continue;
            }
            if (fastTry.getCatches() == null || fastTry.getCatches().size() != 1) {
                continue;
            }
            if (fastTry.getCatches().get(0).instructions() == null || fastTry.getCatches().get(0).instructions().size() != 1) {
                continue;
            }
            if (!(fastTry.getCatches().get(0).instructions().get(0) instanceof Invokevirtual)) {
                continue;
            }
            Invokevirtual catchInstruction = (Invokevirtual) fastTry.getCatches().get(0).instructions().get(0);
            if (!(catchInstruction.getObjectref() instanceof ALoad)) {
                continue;
            }
            ALoad aLoad2 = (ALoad) catchInstruction.getObjectref();
            if (((ALoad) secondIf.getValue()).getIndex() != aLoad2.getIndex()) {
                continue;
            }
            if (!(fastTry.getInstructions().get(0) instanceof InvokeNoStaticInstruction)) {
                continue;
            }
            InvokeNoStaticInstruction tryInstruction = (InvokeNoStaticInstruction) fastTry.getInstructions().get(0);
            if (!(tryInstruction.getObjectref() instanceof ALoad)) {
                continue;
            }
            ALoad aLoad1 = (ALoad) tryInstruction.getObjectref();
            if (((ALoad) firstIf.getValue()).getIndex() != aLoad1.getIndex()) {
                continue;
            }
            LocalVariable lv1 = localVariables.getLocalVariableWithIndexAndOffset(aLoad1.getIndex(), aLoad1.getOffset());
            LocalVariable lv2 = localVariables.getLocalVariableWithIndexAndOffset(aLoad2.getIndex(), aLoad2.getOffset());
            lv1.setTryResources(this);
            ConstantCP catchInstructionMethodref = cp.getConstantMethodref(catchInstruction.getIndex());
            ConstantClass catchInstructionClass = cp.getConstantClass(catchInstructionMethodref.getClassIndex());
            ConstantNameAndType catchInstructionMethod = cp.getConstantNameAndType(catchInstructionMethodref.getNameAndTypeIndex());
            String catchInstructionMethodName = cp.getConstantUtf8(catchInstructionMethod.getNameIndex());
            String catchInstructionMethodDesc = cp.getConstantUtf8(catchInstructionMethod.getSignatureIndex());
            String catchInstructionClassName = cp.getConstantUtf8(catchInstructionClass.getNameIndex());
            if ("java/lang/Throwable".equals(catchInstructionClassName) 
                && "addSuppressed".equals(catchInstructionMethodName) 
                && "(Ljava/lang/Throwable;)V".equals(catchInstructionMethodDesc)) {
                lv2.setThrowableFromTryResources(true);
                if (lv2.isExceptionOrThrowable(cp)) {
                    lv2.setToBeRemoved(true);
                }
            }
            instructions.remove(i);
            instructions.remove(i-1);
            instructions.remove(i-2);
            instructions.remove(i-3);
            return true;
        }
        return false;
    }

    public void addResource(StoreInstruction si, LocalVariable lv) {
        resources.add(new FastDeclaration(si.getOffset(), si.getLineNumber(), lv, si));
        lv.setDeclarationFlag(true);
    }

    public List<Instruction> getResources() {
        return resources;
    }

    public boolean processTryResources() {
        boolean processed = false;
        List<Instruction> instructions = getInstructions();
        if (instructions.size() == 1) {
            Iterator<Instruction> iterator = instructions.iterator();
            Instruction instruction = iterator.next();
            if (instruction instanceof FastTry) {
                FastTry fastTry = (FastTry) instruction;
                if (fastTry.isResourceOnly()) {
                    iterator.remove();
                    resources.addAll(fastTry.getResources());
                    instructions.addAll(fastTry.getInstructions());
                    processed = true;
                }
            }
        }
        return processed;
    }

    public void removeOutOfBoundsInstructions(int maxLineNumber) {
        if (maxLineNumber <= 0) {
            return;
        }

        // Remove try instructions that are out of bounds and should be found in finally instructions
        instructions.removeIf(tryInstr -> tryInstr.getLineNumber() >= maxLineNumber);
        for (Instruction instruction : instructions) {
            if (instruction instanceof FastTry) {
                FastTry nestedTry = (FastTry) instruction;
                nestedTry.removeOutOfBoundsInstructions(maxLineNumber);
            }
        }
        // Remove catch instructions that are out of bounds and should be found in finally instructions
        for (FastCatch fastCatch : catches) {
            fastCatch.instructions().removeIf(catchInstr -> catchInstr.getLineNumber() >= maxLineNumber);
        }
    }

    public boolean isResourceOnly() {
        return hasResource() && !hasCatch() && !hasFinally();
    }

    public boolean hasResource() {
        return !resources.isEmpty();
    }

    public boolean hasFinally() {
        return finallyInstructions != null && !finallyInstructions.isEmpty();
    }

    public boolean hasCatch() {
        return catches != null && !catches.isEmpty();
    }

    public int minCatchLineNumber() {
        int minCatchLineNumber = Integer.MAX_VALUE;
        if (hasCatch()) {
            for (FastCatch fastCatch : catches) {
                minCatchLineNumber = Math.min(minCatchLineNumber, fastCatch.minLineNumber());
            }
        }
        return minCatchLineNumber;
    }
}
