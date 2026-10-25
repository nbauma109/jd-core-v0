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
package jd.core.model.instruction.bytecode.instruction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jd.core.model.classfile.LocalVariable;

/**
 * Selector of a switch on patterns: the value tested by the {@code typeSwitch} or
 * {@code enumSwitch} bootstrap method. The switch itself works on the index of
 * the first matching label, which is the key of each case.
 */
public class PatternSwitchSelector extends ALoad
{
    private final int[] labels;
    private final boolean enumSwitch;
    private final int restartIndexVariable;
    private final Map<Integer, LocalVariable> bindings = new HashMap<>();
    private final Map<Integer, List<LocalVariable>> recordComponents = new HashMap<>();
    private final Map<Integer, Instruction> guards = new HashMap<>();
    private final Map<Integer, Integer> bodyOffsets = new HashMap<>();

    /**
     * @param labels constant pool indexes of the labels, in the order of the keys
     * @param restartIndexVariable variable holding the index to restart from, or -1
     */
    public PatternSwitchSelector(ALoad selector, int[] labels, boolean enumSwitch, int restartIndexVariable)
    {
        super(selector.getOpcode(), selector.getOffset(), selector.getLineNumber(), selector.getIndex());
        this.labels = labels;
        this.enumSwitch = enumSwitch;
        this.restartIndexVariable = restartIndexVariable;
    }

    /** Constant pool index of the label of the given key, or -1 (case null or out of range). */
    public int getLabelConstantIndex(int key)
    {
        return key >= 0 && key < labels.length ? labels[key] : -1;
    }

    public boolean isEnumSwitch()
    {
        return enumSwitch;
    }

    public int getRestartIndexVariable()
    {
        return restartIndexVariable;
    }

    /** Variable bound by the pattern of the case with the given key. */
    public Map<Integer, LocalVariable> getBindings()
    {
        return bindings;
    }

    /** Variables bound by the components of the record pattern of the case with the given key. */
    public Map<Integer, List<LocalVariable>> getRecordComponents()
    {
        return recordComponents;
    }

    /** Condition of the 'when' clause of the case with the given key. */
    public Map<Integer, Instruction> getGuards()
    {
        return guards;
    }

    /**
     * Offset where the code of the body of the case with the given key starts, for the cases
     * beginning with the matching of nested patterns or with a guard.
     */
    public Map<Integer, Integer> getBodyOffsets()
    {
        return bodyOffsets;
    }
}
