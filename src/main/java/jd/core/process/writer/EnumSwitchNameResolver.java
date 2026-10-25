package jd.core.process.writer;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;

/**
 * Resolves enum constant names for a case label, for both the pre-JDK 21
 * switch-map pattern and the direct-ordinal pattern JDK 21+ javac emits.
 * Shared between the classic switch statement writer and the switch
 * expression writer.
 */
public final class EnumSwitchNameResolver
{
    private EnumSwitchNameResolver()
    {
    }

    public record EnumSwitchNames(String internalEnumName, Map<Integer, String> namesByKey)
    {
        public String nameForKey(int key)
        {
            return namesByKey.get(key);
        }
    }

    /** Key of the synthetic $SwitchMap$... field/method for a switch-map array load, or null. */
    public static String switchMapKey(ClassFile classFile, ArrayLoadInstruction ali)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantNameAndType cnat;

        if (ali.getArrayref().getOpcode() == Const.INVOKESTATIC)
        {
            Invokestatic is = (Invokestatic)ali.getArrayref();
            ConstantCP cmr = constants.getConstantMethodref(is.getIndex());
            cnat = constants.getConstantNameAndType(cmr.getNameAndTypeIndex());
        }
        else if (ali.getArrayref().getOpcode() == Const.GETSTATIC)
        {
            GetStatic gs = (GetStatic)ali.getArrayref();
            ConstantFieldref cfr = constants.getConstantFieldref(gs.getIndex());
            cnat = constants.getConstantNameAndType(cfr.getNameAndTypeIndex());
        }
        else
        {
            return null;
        }

        return constants.getConstantUtf8(cnat.getNameIndex());
    }

    public static String internalEnumName(ClassFile classFile, Invokevirtual ordinalCall)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP cmr = constants.getConstantMethodref(ordinalCall.getIndex());
        return constants.getConstantClassName(cmr.getClassIndex());
    }

    /** Pre-JDK 21 javac, and ECJ: names come from the compiler-generated switch-map table. */
    public static EnumSwitchNames fromSwitchMap(ClassFile classFile, String switchMapKey, String internalEnumName)
    {
        ClassFile switchMapOwner = classFile.getSwitchMapOwner(switchMapKey);
        List<Integer> switchMap = switchMapOwner.getSwitchMaps().get(switchMapKey);
        ConstantPool ownerConstants = switchMapOwner.getConstantPool();

        Map<Integer, String> namesByKey = new HashMap<>();
        for (int i = 0; i < switchMap.size(); i++)
        {
            Integer valueIndex = switchMap.get(i);
            if (valueIndex > 0)
            {
                namesByKey.put(i + 1, ownerConstants.getConstantUtf8(valueIndex));
            }
        }

        return new EnumSwitchNames(internalEnumName, namesByKey);
    }

    /** JDK 21+ javac: switches directly on the ordinal value, so names come from the enum's own field order. */
    public static EnumSwitchNames fromOrdinal(ClassFile classFile, String internalEnumName)
    {
        Map<Integer, String> namesByKey = new HashMap<>();
        ClassFile enumClassFile = classFile.resolveClassFile(internalEnumName);
        ConstantPool enumConstants = enumClassFile.getConstantPool();
        int ordinal = 0;
        for (Field field : enumClassFile.getFields())
        {
            if ((field.getAccessFlags() & Const.ACC_ENUM) != 0)
            {
                namesByKey.put(ordinal, enumConstants.getConstantUtf8(field.getNameIndex()));
                ordinal++;
            }
        }

        return new EnumSwitchNames(internalEnumName, namesByKey);
    }
}
