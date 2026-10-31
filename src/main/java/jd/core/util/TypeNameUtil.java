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

import org.jd.core.v1.util.StringConstants;

/*
 * internalPath:        basic/data/Test.class
 * internalClassName:    basic/data/Test
 * qualifiedClassName:    basic.data.Test
 * internalPackageName:    basic/data
 * packageName:            basic.data
 */
public final class TypeNameUtil
{
    private TypeNameUtil() {
    }

    /**
     * Name of a local type as written in the source: the compiler prefixes it
     * with a number ({@code Outer$1Local}), which is not part of the name.
     * Anonymous classes ({@code Outer$1}) are left unchanged.
     */
    public static String localTypeSimpleName(String name)
    {
        int start = 0;
        while (start < name.length() && Character.isDigit(name.charAt(start))) {
            start++;
        }
        return start == 0 || start == name.length() ? name : name.substring(start);
    }

    /** Removes the number prefix of every local type found in a dotted type name. */
    public static String localTypeQualifiedName(String qualifiedName)
    {
        if (qualifiedName.indexOf('.') == -1) {
            return localTypeSimpleName(qualifiedName);
        }
        StringBuilder result = new StringBuilder(qualifiedName.length());
        for (String part : qualifiedName.split("\\.", -1)) {
            if (!result.isEmpty()) {
                result.append('.');
            }
            result.append(localTypeSimpleName(part));
        }
        return result.toString();
    }

    public static String internalTypeNameToInternalPackageName(String path)
    {
        int index = path.lastIndexOf(StringConstants.INTERNAL_PACKAGE_SEPARATOR);
        return index == -1 ? "" : path.substring(0, index);
    }

    public static String internalTypeNameToQualifiedTypeName(String path)
    {
        return path.replace(StringConstants.INTERNAL_PACKAGE_SEPARATOR,
                            StringConstants.PACKAGE_SEPARATOR)
                   .replace(StringConstants.INTERNAL_INNER_SEPARATOR,
                            StringConstants.INNER_SEPARATOR);
    }
}
