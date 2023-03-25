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
package jd.core.util;

import org.apache.bcel.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jd.core.model.instruction.bytecode.ByteCodeConstants;

public final class SignatureUtil
{
    /*
     * Keep letters in order for binary search
     */
    private static final char[] PRIMITIVE_SIGNATURES    = {'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z'};
    private static final char[] INTEGER_SIGNATURES      = {'B', 'C', 'I', 'S'};
    private static final char[] BYTE_SHORT_SIGNATURES   = {'B', 'S'};
    private static final char[] OBJECT_TYPE_SIGNATURES  = {'L', 'T'};
    private static final char[] OBJECT_SIGNATURE        = {'L'};

    private SignatureUtil() {
        super();
    }

    public static int countDimensionOfArray(String signature)
    {
        int index = 0;
        int length = signature.length();

        // Comptage du nombre de dimensions : '[[?' ou '[L[?;'
        if (signature.charAt(index) == '[')
        {
            while (++index < length)
            {
                if (signature.charAt(index) == 'L' &&
                    index+1 < length &&
                    signature.charAt(index+1) == '[')
                {
                    index++;
                    length--;
                }
                else if (signature.charAt(index) != '[')
                {
                    break;
                }
            }
        }

        return index;
    }

    /**
     * @see SignatureAnalyzer.SignatureAnalyzer(...)
     */
    public static int skipSignature(char[] caSignature, int length, int index)
    {
        char c;

        while (true)
        {
            // Affichage des dimensions du tableau : '[[?' ou '[L[?;'
            if (caSignature[index] == '[')
            {
                while (++index < length)
                {
                    if (caSignature[index] == 'L' &&
                        index+1 < length &&
                        caSignature[index+1] == '[')
                    {
                        index++;
                        length--;
                    }
                    else if (caSignature[index] != '[')
                    {
                        break;
                    }
                }
            }

            switch(caSignature[index])
            {
            case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'V', 'Z', '*', 'X', 'Y' :
                index++;
                break;
            case 'L', '.' :
                index++;
                c = '.';

                // Recherche de ; ou de <
                while (index < length)
                {
                    c = caSignature[index];
                    if (c == ';' || c == '<') {
                        break;
                    }
                    index++;
                }

                if (c == '<')
                {
                    index = skipSignature(caSignature, length, index+1);

                    while (caSignature[index] != '>')
                    {
                        index = skipSignature(caSignature, length, index);
                    }

                    // pass '>'
                    index++;
                }

                // pass ';'
                if (caSignature[index] == ';') {
                    index++;
                }
                break;
            case 'T' :
                index = CharArrayUtil.indexOf(caSignature, ';', index+1) + 1;
                break;
            case '-', '+' :
                index = skipSignature(caSignature, length, index+1);
                break;
            //default:
                // DEBUG
                //new Throwable(
                //    "SignatureWriter.writeSignature: invalid signature '" +
                //    String.valueOf(caSignature) + "'").printStackTrace();
                // DEBUG
            }

            if (index >= length || caSignature[index] != '.') {
                break;
            }
        }

        return index;
    }

    public static String getSignatureFromType(int type)
    {
        return switch (type)
        {
        case Const.T_BOOLEAN -> "Z";
        case Const.T_CHAR    -> "C";
        case Const.T_FLOAT   -> "F";
        case Const.T_DOUBLE  -> "D";
        case Const.T_BYTE    -> "B";
        case Const.T_SHORT   -> "S";
        case Const.T_INT     -> "I";
        case Const.T_LONG    -> "J";
        default              -> null;
        };
    }

    public static int getTypeFromSignature(String signature)
    {
        if (signature.length() != 1) {
            return 0;
        }

        return switch (signature.charAt(0))
        {
        case 'Z' -> Const.T_BOOLEAN;
        case 'C' -> Const.T_CHAR;
        case 'F' -> Const.T_FLOAT;
        case 'D' -> Const.T_DOUBLE;
        case 'B' -> Const.T_BYTE;
        case 'S' -> Const.T_SHORT;
        case 'I' -> Const.T_INT;
        case 'J' -> Const.T_LONG;
        default  -> 0;
        };
    }

    public static boolean isGenericSignature(String signature)
    {
        return signature.charAt(0) == 'T';
    }

    public static boolean isPrimitiveSignature(String signature)
    {
        return isPrimitiveSignature(signature, PRIMITIVE_SIGNATURES);
    }

    public static boolean isIntegerSignature(String signature)
    {
        return isPrimitiveSignature(signature, INTEGER_SIGNATURES);
    }

    public static boolean isByteOrShortSignature(String signature)
    {
        return isPrimitiveSignature(signature, BYTE_SHORT_SIGNATURES);
    }

    private static boolean isPrimitiveSignature(String signature, char[] letters)
    {
        if (signature == null || signature.length() != 1) {
            return false;
        }
        return Arrays.binarySearch(letters, signature.charAt(0)) >= 0;
    }

    public static boolean isObjectTypeSignature(String signature)
    {
        return isObjectSignature(signature, OBJECT_TYPE_SIGNATURES);
    }

    public static boolean isObjectSignature(String signature)
    {
        return isObjectSignature(signature, OBJECT_SIGNATURE);
    }

    private static boolean isObjectSignature(String signature, char[] letters)
    {
        if (signature == null || signature.length() <= 2) {
            return false;
        }
        return Arrays.binarySearch(letters, signature.charAt(0)) >= 0;
    }

    public static String getInternalName(String signature)
    {
        char[] caSignature = signature.toCharArray();
        int length = signature.length();
        int beginIndex = 0;

        while (beginIndex < length && caSignature[beginIndex] == '[') {
            beginIndex++;
        }

        if (beginIndex < length && caSignature[beginIndex] == 'L')
        {
            beginIndex++;
            length--;
            return CharArrayUtil.substring(caSignature, beginIndex, length);
        }
        return beginIndex == 0 ? signature :
            CharArrayUtil.substring(caSignature, beginIndex, length);
    }

    public static String cutArrayDimensionPrefix(String signature)
    {
        int beginIndex = 0;

        while (signature.charAt(beginIndex) == '[') {
            beginIndex++;
        }

        return signature.substring(beginIndex);
    }

    public static int getArrayDimensionCount(String signature)
    {
        int beginIndex = 0;

        while (signature.charAt(beginIndex) == '[') {
            beginIndex++;
        }

        return beginIndex;
    }

    public static String getInnerName(String signature)
    {
        signature = cutArrayDimensionPrefix(signature);
        if (isObjectTypeSignature(signature))
        {
            return signature.substring(1, signature.length()-1);
        }
        return signature;
    }

    public static List<String> getParameterSignatures(
            String methodSignature)
    {
        char[] caSignature = methodSignature.toCharArray();
        int length = caSignature.length;
        List<String> parameterTypes = new ArrayList<>(1);
        int index = CharArrayUtil.indexOf(caSignature, '(', 0);

        if (index != -1)
        {
            // pass '('
            index++;

            // Arguments
            while (caSignature[index] != ')')
            {
                int newIndex = skipSignature(caSignature, length, index);
                parameterTypes.add(methodSignature.substring(index, newIndex));
                index = newIndex;
            }
        }

        return parameterTypes;
    }

    public static String getMethodReturnedSignature(String signature)
    {
        int index = signature.indexOf(')');
        if (index == -1) {
            return null;
        }

        return signature.substring(index + 1);
    }

    public static int getParameterSignatureCount(String methodSignature)
    {
        char[] caSignature = methodSignature.toCharArray();
        int length = caSignature.length;
        int index = CharArrayUtil.indexOf(caSignature, '(', 0);
        int count = 0;

        if (index != -1)
        {
            // pass '('
            index++;

            // Arguments
            while (caSignature[index] != ')')
            {
                int newIndex = skipSignature(caSignature, length, index);
                index = newIndex;
                count++;
            }
        }

        return count;
    }

    public static int createTypesBitField(String signature)
    {
        /*
         * Pour une constante de type 'signature', les types de variable
         * possible est retournée.
         */
        return switch (signature.charAt(0))
        {
        case 'I' -> ByteCodeConstants.TBF_INT_INT;
        case 'S' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT;
        case 'B' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE | ByteCodeConstants.TBF_INT_BOOLEAN;
        case 'C' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_CHAR;
        case 'X' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE | ByteCodeConstants.TBF_INT_CHAR | ByteCodeConstants.TBF_INT_BOOLEAN;
        case 'Y' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE | ByteCodeConstants.TBF_INT_CHAR;
        case 'Z' -> ByteCodeConstants.TBF_INT_BOOLEAN;
        default -> 0;
        };
    }

    public static int createArgOrReturnBitFields(String signature)
    {
        /*
         * Pour un argument de type 'signature', les types de variable possible
         * est retournée.
         */
        return switch (signature.charAt(0))
        {
        case 'I' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE | ByteCodeConstants.TBF_INT_CHAR;
        case 'S' -> ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE;
        case 'B' -> ByteCodeConstants.TBF_INT_BYTE;
        case 'C' -> ByteCodeConstants.TBF_INT_CHAR;
        case 'X' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE | ByteCodeConstants.TBF_INT_CHAR | ByteCodeConstants.TBF_INT_BOOLEAN;
        case 'Y' -> ByteCodeConstants.TBF_INT_INT | ByteCodeConstants.TBF_INT_SHORT | ByteCodeConstants.TBF_INT_BYTE | ByteCodeConstants.TBF_INT_CHAR;
        case 'Z' -> ByteCodeConstants.TBF_INT_BOOLEAN;
        default  -> 0;
        };
    }

    public static String getSignatureFromTypesBitField(int typesBitField)
    {
        /*
         * Lorsqu'un choix est possible, le plus 'gros' type est retourné.
         */
        if ((typesBitField & ByteCodeConstants.TBF_INT_INT) != 0) {
            return "I";
        }
        if ((typesBitField & ByteCodeConstants.TBF_INT_SHORT) != 0) {
            return "S";
        }
        if ((typesBitField & ByteCodeConstants.TBF_INT_CHAR) != 0) {
            return "C";
        }
        if ((typesBitField & ByteCodeConstants.TBF_INT_BYTE) != 0) {
            return "B";
        }
        if ((typesBitField & ByteCodeConstants.TBF_INT_BOOLEAN) != 0) {
            return "Z";
        }
        return "I";
    }

    public static String createTypeName(String signature)
    {
        if (signature.isEmpty()) {
            return signature;
        }

        switch(signature.charAt(0))
        {
        case '[':
            return signature;
        case 'L', 'T':
            if (signature.charAt(signature.length()-1) == ';') {
                return signature;
            }
            // intended fall through
        default:
            return "L" + signature + ';';
        }
    }
}
