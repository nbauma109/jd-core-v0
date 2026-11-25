package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.bcel.Const;
import org.junit.Test;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.process.analyzer.instruction.bytecode.factory.GotoFactory;

/** The value of a switch expression stays on the stack when only a throwing branch follows its last jump. */
public class GotoFactoryTest {

    private static final int GOTO_OFFSET = 4;
    private static final int THROWING_BRANCH = GOTO_OFFSET + 3;

    @Test
    public void testValueStoredInWideVariableAfterThrowingBranch() {
        // switch ... 4: goto store; 7: switch inside the throwing branch; athrow; store: wide istore 256
        byte[] code = code((byte) Const.WIDE, (byte) Const.ISTORE, (byte) 1, (byte) 0);

        Deque<Instruction> stack = new ArrayDeque<>();
        List<Instruction> list = jump(code, stack, THROWING_BRANCH);

        assertEquals(1, stack.size());
        assertTrue(list.stream().noneMatch(TernaryOpStore.class::isInstance));
    }

    @Test
    public void testValueUsedByAnotherInstructionIsNotKeptForASwitchExpression() {
        byte[] code = code((byte) Const.ARRAYLENGTH);

        Deque<Instruction> stack = new ArrayDeque<>();
        List<Instruction> list = jump(code, stack, THROWING_BRANCH);

        // No condition precedes the jump: the value stays, as for any switch
        assertEquals(1, stack.size());
        assertEquals(2, list.size());
    }

    /** A jump to the instructions following a branch made of a table switch and a throw. */
    private static byte[] code(byte... consumer) {
        int tableSwitch = THROWING_BRANCH;
        int padding = 3 - tableSwitch % 4;
        // opcode, padding, default, low, high, one offset
        int athrow = tableSwitch + 1 + padding + 16;
        int target = athrow + 1;
        byte[] code = new byte[target + consumer.length];
        code[GOTO_OFFSET] = (byte) Const.GOTO;
        code[GOTO_OFFSET + 2] = (byte) (target - GOTO_OFFSET);
        code[tableSwitch] = (byte) Const.TABLESWITCH;
        code[athrow] = (byte) Const.ATHROW;
        System.arraycopy(consumer, 0, code, target, consumer.length);
        return code;
    }

    private static List<Instruction> jump(byte[] code, Deque<Instruction> stack, int branch) {
        List<Instruction> list = new ArrayList<>();
        list.add(new Switch(Const.TABLESWITCH, 0, 1, new IConst(ByteCodeConstants.ICONST, 0, 1, 0), branch,
                new int[0]));
        stack.push(new IConst(ByteCodeConstants.ICONST, 1, 1, 1));
        new GotoFactory().create(null, null, list, new ArrayList<>(), stack, code, GOTO_OFFSET, 1, new boolean[code.length]);
        return list;
    }
}
