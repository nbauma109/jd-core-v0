/*******************************************************************************
 * Copyright (C) 2007-2023 Emmanuel Dupuy GPLv3 and other contributors
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

import org.apache.bcel.classfile.Signature;

import java.util.Optional;

public interface SignatureInfo {

    default int getSignatureIndex() {
        return Optional.ofNullable(getAttributeSignature())
                       .map(Signature::getSignatureIndex)
                       .orElseGet(this::getDescriptorIndex);
    }

    default String getSignature(ConstantPool cp) {
        return cp.getConstantUtf8(getSignatureIndex());
    }

    Signature getAttributeSignature();

    int getDescriptorIndex();
}
