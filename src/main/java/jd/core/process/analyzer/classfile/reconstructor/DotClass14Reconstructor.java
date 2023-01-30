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
package jd.core.process.analyzer.classfile.reconstructor;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantString;
import org.jd.core.v1.model.classfile.constant.ConstantMethodref;
import org.jd.core.v1.util.StringConstants;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.visitor.ReplaceGetStaticVisitor;

/*
 * Reconstruction du mot cle '.class' depuis les instructions generees par le
 * JDK 1.4 de SUN :
 * ...
 * ifnotnull( getstatic( current or outer class, 'class$...', Class ) )
 *  dupstore( invokestatic( current or outer class, 'class$', nom de la classe ) )
 *  putstatic( current class, 'class$...', Class, dupload )
 *  ternaryOpStore( dupload )
 *  goto
 * ???( getstatic( class, 'class$...' ) )
 * ...
 */
public final class DotClass14Reconstructor
{
    private DotClass14Reconstructor() {
        super();
    }

    public static void reconstruct(
        ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list)
    {
        int i = list.size();

        if  (i < 6) {
            return;
        }

        i -= 5;
        ConstantPool constants = classFile.getConstantPool();

        while (i-- > 0)
        {
            Instruction instruction = list.get(i);

            if (instruction.getOpcode() != ByteCodeConstants.IFXNULL) {
                continue;
            }

            IfInstruction ii = (IfInstruction)instruction;

            if (ii.getValue().getOpcode() != Const.GETSTATIC) {
                continue;
            }

            int jumpOffset = ii.getJumpOffset();

            instruction = list.get(i+1);

            if (instruction.getOpcode() != ByteCodeConstants.DUPSTORE) {
                continue;
            }

            DupStore ds = (DupStore)instruction;

            if (ds.getObjectref().getOpcode() != Const.INVOKESTATIC) {
                continue;
            }

            Invokestatic is = (Invokestatic)ds.getObjectref();

            if (is.getArgs().size() != 1) {
                continue;
            }

            instruction = is.getArgs().get(0);

            if (instruction.getOpcode() != Const.LDC) {
                continue;
            }

            instruction = list.get(i+2);

            if (instruction.getOpcode() != Const.PUTSTATIC) {
                continue;
            }

            PutStatic ps = (PutStatic)instruction;

            if (ps.getValueref().getOpcode() != ByteCodeConstants.DUPLOAD ||
                ds.getOffset() != ps.getValueref().getOffset()) {
                continue;
            }

            instruction = list.get(i+3);

            if (instruction.getOpcode() != ByteCodeConstants.TERNARYOPSTORE) {
                continue;
            }

            TernaryOpStore tos = (TernaryOpStore)instruction;

            if (tos.getObjectref().getOpcode() != ByteCodeConstants.DUPLOAD ||
                ds.getOffset() != tos.getObjectref().getOffset()) {
                continue;
            }

            instruction = list.get(i+4);

            if (instruction.getOpcode() != Const.GOTO) {
                continue;
            }

            Goto g = (Goto)instruction;
            instruction = list.get(i+5);

            if (g.getOffset() >= jumpOffset || jumpOffset > instruction.getOffset()) {
                continue;
            }

            GetStatic gs = (GetStatic)ii.getValue();

            if (ps.getIndex() != gs.getIndex()) {
                continue;
            }

            ConstantFieldref cfr = constants.getConstantFieldref(gs.getIndex());

            if (searchMatchingClassFile(cfr.getClassIndex(), classFile) == null) {
                continue;
            }

            ConstantNameAndType cnatField = constants.getConstantNameAndType(
                    cfr.getNameAndTypeIndex());

            String descriptorField =
                constants.getConstantUtf8(cnatField.getSignatureIndex());

            if (! StringConstants.INTERNAL_CLASS_SIGNATURE.equals(descriptorField)) {
                continue;
            }

            String nameField = constants.getConstantUtf8(cnatField.getNameIndex());

            if (!nameField.startsWith(StringConstants.CLASS_DOLLAR) &&
                !nameField.startsWith(StringConstants.ARRAY_DOLLAR)) {
                continue;
            }

            ConstantMethodref cmr = constants.getConstantMethodref(is.getIndex());

            ClassFile matchingClassFile =
                searchMatchingClassFile(cmr.getClassIndex(), classFile);
            if (matchingClassFile == null) {
                continue;
            }

            ConstantNameAndType cnatMethod =
                constants.getConstantNameAndType(cmr.getNameAndTypeIndex());
            String nameMethod =
                constants.getConstantUtf8(cnatMethod.getNameIndex());

            if (! StringConstants.CLASS_DOLLAR.equals(nameMethod)) {
                continue;
            }

            Ldc ldc = (Ldc)is.getArgs().get(0);
            Constant cv = constants.getConstantValue(ldc.getIndex());

            if (cv.getTag() != Const.CONSTANT_String) {
                continue;
            }
            // Trouvé !
            ConstantString cs = (ConstantString)cv;
            String signature = constants.getConstantUtf8(cs.getStringIndex());

            String internalName = signature.replace(
                StringConstants.PACKAGE_SEPARATOR,
                StringConstants.INTERNAL_PACKAGE_SEPARATOR);

            referenceMap.add(internalName);

            // Ajout du nom interne
            int index = constants.addConstantUtf8(internalName);
            // Ajout d'une nouvelle classe
            index = constants.addConstantClass(index);
            ldc = new Ldc(
                Const.LDC, ii.getOffset(),
                ii.getLineNumber(), index);

            // Remplacement de l'instruction GetStatic par l'instruction Ldc
            ReplaceGetStaticVisitor visitor =
                new ReplaceGetStaticVisitor(gs.getIndex(), ldc);

            visitor.visit(instruction);

            // Retrait de l'instruction Goto
            list.remove(i+4);
            // Retrait de l'instruction TernaryOpStore
            list.remove(i+3);
            // Retrait de l'instruction PutStatic
            list.remove(i+2);
            // Retrait de l'instruction DupStore
            list.remove(i+1);
            // Retrait de l'instruction IfNotNull
            list.remove(i);

            if (matchingClassFile == classFile)
            {
                // Recherche de l'attribut statique et ajout de l'attribut SYNTHETIC
                Field[] fields = classFile.getFields();
                int j = fields.length;

                while (j-- > 0)
                {
                    Field field = fields[j];

                    if (field.getNameIndex() == cnatField.getNameIndex())
                    {
                        field.setAccessFlags(field.getAccessFlags() | Const.ACC_SYNTHETIC);
                        break;
                    }
                }

                // Recherche de la méthode statique et ajout de l'attribut SYNTHETIC
                Method[] methods = classFile.getMethods();
                j = methods.length;

                while (j-- > 0)
                {
                    Method method = methods[j];

                    if (method.getNameIndex() == cnatMethod.getNameIndex())
                    {
                        method.setAccessFlags(method.getAccessFlags() | Const.ACC_SYNTHETIC);
                        break;
                    }
                }
            }
            else
            {
                // Recherche de l'attribut statique et ajout de l'attribut SYNTHETIC
                ConstantPool matchingConstants =
                    matchingClassFile.getConstantPool();
                Field[] fields = matchingClassFile.getFields();
                int j = fields.length;

                while (j-- > 0)
                {
                    Field field = fields[j];

                    if (nameField.equals(
                            matchingConstants.getConstantUtf8(field.getNameIndex())))
                    {
                        field.setAccessFlags(field.getAccessFlags() | Const.ACC_SYNTHETIC);
                        break;
                    }
                }

                // Recherche de la méthode statique et ajout de l'attribut SYNTHETIC
                Method[] methods = matchingClassFile.getMethods();
                j = methods.length;

                while (j-- > 0)
                {
                    Method method = methods[j];

                    if (nameMethod.equals(
                            matchingConstants.getConstantUtf8(method.getNameIndex())))
                    {
                        method.setAccessFlags(method.getAccessFlags() | Const.ACC_SYNTHETIC);
                        break;
                    }
                }
            }
        }
    }

    private static ClassFile searchMatchingClassFile(
        int classIndex, ClassFile classFile)
    {
        if (classIndex == classFile.getThisClassIndex()) {
            return classFile;
        }

        String className =
            classFile.getConstantPool().getConstantClassName(classIndex);

        for (;;)
        {
            classFile = classFile.getOuterClass();

            if (classFile == null) {
                return null;
            }

            if (classFile.getThisClassName().equals(className)) {
                return classFile;
            }
        }
    }
}
