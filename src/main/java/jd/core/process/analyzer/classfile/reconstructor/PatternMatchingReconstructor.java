/*******************************************************************************
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
package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;

import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;

/**
 * Reconstructs the pattern matching of 'instanceof' compiled by javac:
 *
 * <pre>
 * if (o instanceof String) goto L; s = (String) o; ...
 * </pre>
 *
 * The store of the binding variable is removed from the instructions, so that the
 * conditions around it can be combined again, and the variable is attached to the
 * 'instanceof' operator.
 */
public final class PatternMatchingReconstructor {

    private PatternMatchingReconstructor() {
    }

    public static void reconstruct(Method method, List<Instruction> list) {
        LocalVariables localVariables = method.getLocalVariables();
        if (localVariables == null) {
            return;
        }
        for (int i = list.size() - 2; i >= 0; i--) {
            if (list.get(i) instanceof IfInstruction test && test.getValue() instanceof InstanceOf instanceOf
                    && instanceOf.getPatternVariable() == null
                    && list.get(i + 1) instanceof AStore store && isBinding(instanceOf, store)) {
                LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(store.getIndex(), store.getOffset());
                if (lv != null) {
                    instanceOf.setPatternVariable(lv);
                    lv.setDeclarationFlag(true);
                    lv.setPatternBinding(true);
                    list.remove(i + 1);
                }
            }
        }
    }

    private static boolean isBinding(InstanceOf instanceOf, AStore store) {
        return store.getValueref() instanceof CheckCast cast
                && cast.getIndex() == instanceOf.getIndex()
                && cast.getObjectref() instanceof ALoad castSource
                && instanceOf.getObjectref() instanceof ALoad tested
                && castSource.getIndex() == tested.getIndex();
    }
}
