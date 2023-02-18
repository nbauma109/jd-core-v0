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
package jd.core.model.reference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReferenceMap
{
    private final Map<String, Reference> references;
    private final Set<String> simpleInnerNames;

    public ReferenceMap()
    {
        this.references = new HashMap<>();
        this.simpleInnerNames = new HashSet<>();
    }

    public void add(String internalName)
    {
        if (internalName.indexOf(';') != -1)
        {
            System.err.println(
                "ReferenceMap.add: InvalidParameterException(" +
                internalName + ")");
            // throw new IllegalArgumentException(internalName);
        }
        else
        {
            Reference ref = this.references.get(internalName);

            if (ref == null) {
                this.references.put(internalName, new Reference(internalName));
                int lastIndexOfDollar = internalName.lastIndexOf('$');
                if (lastIndexOfDollar != -1) {
                    this.simpleInnerNames.add(internalName.substring(lastIndexOfDollar + 1));
                }
            } else {
                ref.incCounter();
            }
        }
    }

    public Reference remove(String internalName)
    {
        return this.references.remove(internalName);
    }

    public Collection<Reference> values()
    {
        return this.references.values();
    }

//    public String toString()
//    {
//        return this.references.values().toString();
//    }

    public boolean contains(String internalName)
    {
        return this.references.containsKey(internalName);
    }

    public boolean containsSimpleName(String internalName)
    {
        int lastIndexOfSlash = internalName.lastIndexOf('/');
        if (lastIndexOfSlash != -1) {
            return this.simpleInnerNames.contains(internalName.substring(lastIndexOfSlash + 1));
        }
        return false;
    }
}
