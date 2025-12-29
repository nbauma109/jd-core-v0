/*******************************************************************************
 * Copyright (C) 2025 GPLv3
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
package jd.core.process.analyzer.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantString;
import org.apache.commons.lang3.Validate;
import org.jd.core.v1.util.StringConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Ldc;

public final class StringConcatenationUtil {

    private static final char RECIPE_DYNAMIC_ARGUMENT = 1;   // \u0001
    private static final char RECIPE_BOOTSTRAP_CONSTANT = 2; // \u0002
    private static final int STRING_CONCAT_PRIORITY = 4;

    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private StringConcatenationUtil() {
    }

    public static void enter(ClassFile classFile, LocalVariables lv, int offset, int lineNumber) {
        enter(classFile, lv, offset, lineNumber, null, 0);
    }

    public static void enter(ClassFile classFile,
                             LocalVariables lv,
                             int offset,
                             int lineNumber,
                             int[] bootstrapArguments,
                             int bootstrapConstantsStartIndex) {

        Objects.requireNonNull(classFile, "classFile");
        CONTEXT.set(new Context(classFile, lv, offset, lineNumber, bootstrapArguments, bootstrapConstantsStartIndex));
    }

    public static void exit() {
        CONTEXT.remove();
    }

    public static Instruction create(String recipe, List<Instruction> indyParameters) {
        Context context = requireContext();
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(indyParameters, "indyParameters");
        return createFromRecipe(context, recipe, indyParameters);
    }

    public static Instruction create(List<Instruction> indyParameters) {
        Context context = requireContext();
        Objects.requireNonNull(indyParameters, "indyParameters");
        return createWithoutRecipe(context, indyParameters);
    }

    private static Context requireContext() {
        return Objects.requireNonNull(CONTEXT.get(), "StringConcatenationUtil context is not set. Call enter(...) before create(...), and exit() afterwards.");
    }

    private static Instruction createFromRecipe(Context context, String recipe, List<Instruction> indyParameters) {
        RecipeParseResult parseResult = parseRecipe(context, recipe, indyParameters);
        List<Instruction> parts = parseResult.parts;

        if (parseResult.requiresLeadingEmptyString) {
            parts.add(0, createUtf8StringLiteral(context, ""));
        }

        return foldConcatenation(context, parts);
    }

    private static Instruction createWithoutRecipe(Context ctx, List<Instruction> indyParameters) {
        Objects.requireNonNull(indyParameters, "indyParameters");

        if (containsString(indyParameters)) {
            return foldConcatenation(ctx, indyParameters);
        }

        List<Instruction> parts = new ArrayList<>(indyParameters.size() + 1);
        parts.add(createUtf8StringLiteral(ctx, ""));
        parts.addAll(indyParameters);
        return foldConcatenation(ctx, parts);
    }

    private static boolean containsString(List<Instruction> indyParameters) {
        for (Instruction indyParameter : indyParameters) {
            if (isString(indyParameter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isString(Instruction instruction) {
        Objects.requireNonNull(instruction, "instruction");

        Context ctx = CONTEXT.get();
        String returnedSignature = instruction.getReturnedSignature(ctx.classFile, ctx.localVariables);
        return "Ljava/lang/String;".equals(returnedSignature);
    }

    private static Instruction foldConcatenation(Context context, List<Instruction> parts) {
        Instruction expression = parts.get(0);

        for (int i = 1; i < parts.size(); i++) {
            expression = new BinaryOperatorInstruction(
                    ByteCodeConstants.BINARYOP,
                    context.offset,
                    context.lineNumber,
                    STRING_CONCAT_PRIORITY,
                    StringConstants.INTERNAL_STRING_SIGNATURE,
                    "+",
                    expression,
                    parts.get(i));
        }

        return expression;
    }

    private static RecipeParseResult parseRecipe(Context context, String recipe, List<Instruction> indyParameters) {
        List<Instruction> parts = new ArrayList<>();
        StringBuilder literal = new StringBuilder();

        int dynamicIndex = 0;
        int bootstrapIndex = context.bootstrapConstantsStartIndex;

        boolean hasStringBootstrapConstant = false;

        for (int i = 0; i < recipe.length(); i++) {
            char c = recipe.charAt(i);

            if (c == RECIPE_DYNAMIC_ARGUMENT) {
                flushLiteralIfNeeded(context, literal, parts);
                dynamicIndex = addDynamicArgument(indyParameters, dynamicIndex, parts);
            } else if (c == RECIPE_BOOTSTRAP_CONSTANT) {
                flushLiteralIfNeeded(context, literal, parts);
                BootstrapConstantResult constantResult = addBootstrapConstant(context, bootstrapIndex, parts);
                bootstrapIndex = constantResult.nextBootstrapIndex;
                hasStringBootstrapConstant = hasStringBootstrapConstant || constantResult.isStringConstant;
            } else {
                literal.append(c);
            }
        }

        flushLiteralIfNeeded(context, literal, parts);

        boolean requiresLeadingEmptyString = !containsString(indyParameters);

        return new RecipeParseResult(parts, requiresLeadingEmptyString);
    }

    private static void flushLiteralIfNeeded(Context context, StringBuilder literal, List<Instruction> parts) {
        if (literal.isEmpty()) {
            return;
        }

        parts.add(createUtf8StringLiteral(context, literal.toString()));
        literal.setLength(0);
    }

    private static int addDynamicArgument(List<Instruction> indyParameters, int dynamicIndex, List<Instruction> parts) {
        Validate.isTrue(dynamicIndex < indyParameters.size(), "Recipe expects more dynamic arguments than provided.");

        parts.add(indyParameters.get(dynamicIndex));
        return dynamicIndex + 1;
    }

    private static BootstrapConstantResult addBootstrapConstant(Context context, int bootstrapIndex, List<Instruction> parts) {
        Objects.requireNonNull(context.bootstrapArguments, "Recipe uses bootstrap constants (\\u0002) but no bootstrap arguments were provided.");
        Validate.isTrue(bootstrapIndex < context.bootstrapArguments.length, "Recipe expects more bootstrap constants than provided.");

        int constantPoolIndex = context.bootstrapArguments[bootstrapIndex];
        Constant constant = context.classFile.getConstantPool().getConstantValue(constantPoolIndex);

        boolean isStringConstant = constant instanceof ConstantString;

        parts.add(createConstantLoadInstruction(context, constantPoolIndex, constant));

        return new BootstrapConstantResult(bootstrapIndex + 1, isStringConstant);
    }

    private static Instruction createConstantLoadInstruction(Context context, int constantPoolIndex, Constant constant) {
        return new Ldc(Const.LDC, context.offset, context.lineNumber, constantPoolIndex);
    }

    private static Instruction createUtf8StringLiteral(Context context, String value) {
        int utf8Index = context.classFile.getConstantPool().addConstantUtf8(value);
        return new Ldc(Const.LDC, context.offset, context.lineNumber, utf8Index);
    }

    private record Context(ClassFile classFile,
                        LocalVariables localVariables,
                        int offset,
                        int lineNumber,
                        int[] bootstrapArguments,
                        int bootstrapConstantsStartIndex) {
    }

    private record RecipeParseResult(List<Instruction> parts, boolean requiresLeadingEmptyString) {
    }

    private record BootstrapConstantResult(int nextBootstrapIndex, boolean isStringConstant) {
    }
}
