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
package jd.core.process.analyzer.classfile.reconstructor;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantCP;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.util.UtilConstants;

public class NewInstructionReconstructorBase
{
    protected NewInstructionReconstructorBase() {
    }

    /**
     * Methode permettant l'affichage des variables locales d'une méthode d'une
     * outer class dans une inner class.
     */
    public static void initAnonymousClassConstructorParameterName(
        ClassFile classFile, Method method, InvokeNew invokeNew)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP cmr = constants.getConstantMethodref(invokeNew.getIndex());
        String internalClassName = constants.getConstantClassName(
            cmr.getClassIndex());
        ClassFile innerClassFile =
            classFile.getInnerClassFile(internalClassName);

        if (innerClassFile != null)
        {
            // Initialize inner and anonymous class field names
            ConstantPool innerConstants = innerClassFile.getConstantPool();
            LocalVariables localVariables = method.getLocalVariables();

            int index;
            for (Field innerField : innerClassFile.getFields())
            {
                index = innerField.getAnonymousClassConstructorParameterIndex();

                if (index != UtilConstants.INVALID_INDEX)
                {
                    innerField.setAnonymousClassConstructorParameterIndex(UtilConstants.INVALID_INDEX);
                    Instruction arg = searchInstructionInArgs(invokeNew.getArgs(), index);
                    if (arg != null)
                    {
                        if (arg.getOpcode() == Const.CHECKCAST) {
                            arg = ((CheckCast)arg).getObjectref();
                        }

                        int argOpCode = arg.getOpcode();
                        if (argOpCode == ByteCodeConstants.LOAD
                         || argOpCode == Const.ALOAD
                         || argOpCode == Const.ILOAD) {
                            LocalVariable lv =
                                localVariables
                                    .getLocalVariableWithIndexAndOffset(
                                        ((IndexInstruction)arg).getIndex(),
                                        arg.getOffset());

                            if (lv != null)
                            {
                                // Ajout du nom du parametre au ConstantPool
                                // de la classe anonyme
                                String name =
                                    constants.getConstantUtf8(lv.getNameIndex());
                                innerField.setOuterMethodLocalVariableNameIndex(innerConstants.addConstantUtf8(name));
                                innerField.setConstantPool(innerConstants);
                                // Ajout du flag 'final' sur la variable
                                // locale de la méthode contenant
                                // l'instruction "new"
                                lv.setFinalFlag(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private static Instruction searchInstructionInArgs(List<Instruction> args, int index) {
        return args.stream()
                .filter(IndexInstruction.class::isInstance)
                .filter(ii -> ((IndexInstruction) ii).getIndex() == index)
                .findFirst()
                .orElse(null);
    }
}
