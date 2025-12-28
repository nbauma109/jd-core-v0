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
package jd.core.model.classfile;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.FieldOrMethod;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.util.UtilConstants;

public class Field extends Base implements SignatureInfo
{
    private FieldOrMethod fieldOrMethod;
    private ValueAndMethod valueAndMethod;
    private ConstantPool constantPool;
    private boolean outerLocalVariable;
    /*
     * Attributs pour l'affichage des champs synthetique des classes anonymes.
     * Champs modifié par:
     * 1) ClassFileAnalyzer.AnalyseAndModifyConstructors(...) pour y placer le
     *    numéro (position) du parametre du constructeur initialisant le champs.
     */
    private int anonymousClassConstructorParameterIndex;
    /*
     * 2) NewInstructionReconstructorBase.InitAnonymousClassConstructorParameterName(...)
     *    pour y placer l'index du nom du parametre.
     * 3) SourceWriterVisitor.writeGetField(...) pour afficher le nom du
     *    parameter de la classe englobante.
     */
    private int outerMethodLocalVariableNameIndex;

    public Field(FieldOrMethod fieldOrMethod)
    {
        super(fieldOrMethod.getAccessFlags(), fieldOrMethod.getAttributes());
        this.fieldOrMethod = fieldOrMethod;
        this.setAnonymousClassConstructorParameterIndex(UtilConstants.INVALID_INDEX);
        this.setOuterMethodLocalVariableNameIndex(UtilConstants.INVALID_INDEX);
    }

    public Constant getConstantValue(ConstantPool constants)
    {
        for (Attribute attribute : fieldOrMethod.getAttributes()) {
            if (attribute.getTag() == Const.ATTR_CONSTANT_VALUE)
            {
                ConstantValue acv = (ConstantValue)attribute;
                return constants.getConstantValue(acv.getConstantValueIndex());
            }
        }
        return null;
    }

    public ValueAndMethod getValueAndMethod()
    {
        return valueAndMethod;
    }

    public void setValueAndMethod(Instruction value, Method method)
    {
        this.valueAndMethod = new ValueAndMethod(value, method);
    }

    public int getAnonymousClassConstructorParameterIndex() {
        return anonymousClassConstructorParameterIndex;
    }

    public void setAnonymousClassConstructorParameterIndex(int anonymousClassConstructorParameterIndex) {
        this.anonymousClassConstructorParameterIndex = anonymousClassConstructorParameterIndex;
    }

    public int getOuterMethodLocalVariableNameIndex() {
        return outerMethodLocalVariableNameIndex;
    }

    public void setOuterMethodLocalVariableNameIndex(int outerMethodLocalVariableNameIndex) {
        this.outerMethodLocalVariableNameIndex = outerMethodLocalVariableNameIndex;
    }

    public record ValueAndMethod(Instruction value, Method method) {
    }

    public int getNameIndex() {
        return fieldOrMethod.getNameIndex();
    }

    @Override
    public int getDescriptorIndex() {
        return fieldOrMethod.getSignatureIndex();
    }

    public void setNameIndex(int newNameIndex) {
        fieldOrMethod.setNameIndex(newNameIndex);
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public void setConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    public boolean isOuterLocalVariable() {
        return outerLocalVariable;
    }

    public void setOuterLocalVariable(boolean outerLocalVariable) {
        this.outerLocalVariable = outerLocalVariable;
    }

    public String getName(ConstantPool cp) {
        return cp.getConstantUtf8(fieldOrMethod.getNameIndex());
    }
}
