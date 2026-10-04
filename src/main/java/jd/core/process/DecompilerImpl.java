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
package jd.core.process;

import org.jd.core.v1.api.loader.Loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jd.core.Decompiler;
import jd.core.model.classfile.ClassFile;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.preferences.Preferences;
import jd.core.printer.Printer;
import jd.core.printer.PrinterImpl;
import jd.core.printer.PrinterImpl;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

public class DecompilerImpl implements Decompiler
{
    @Override
    public synchronized void decompile(
            Preferences preferences, Loader loader,
            Printer printer, String internalClassPath)
        throws IOException
    {
        if (!preferences.getRealignmentLineNumber()) {
            layout(preferences, loader, internalClassPath, null).write(loader, printer);
            return;
        }

        // The constants without line number are put after the members only if it aligns more line numbers
        Layout natural = layout(preferences, loader, internalClassPath, new ClassFileLayouter.FieldPlacement(false));
        if (!natural.placement.isMovable()) {
            natural.write(loader, printer);
            return;
        }

        // (the analysis changes the model of the class: each placement is laid out from its own copy)
        Layout moved = layout(preferences, loader, internalClassPath, new ClassFileLayouter.FieldPlacement(true));
        PrinterImpl naturalProbe = new PrinterImpl(preferences, true);
        PrinterImpl movedProbe = new PrinterImpl(preferences, true);
        natural.write(loader, naturalProbe);
        moved.write(loader, movedProbe);
        // (writing consumes the blocks: the chosen placement is laid out again for the printer)
        boolean keepMoved = movedProbe.getMisalignedLineNumberCount() < naturalProbe.getMisalignedLineNumberCount();
        layout(preferences, loader, internalClassPath, new ClassFileLayouter.FieldPlacement(keepMoved)).write(loader, printer);
    }

    private static Layout layout(
            Preferences preferences, Loader loader, String internalClassPath,
            ClassFileLayouter.FieldPlacement placement)
        throws IOException
    {
//long time0 = System.currentTimeMillis();

        // 1) Deserialisation
        ClassFile classFile =
            ClassFileDeserializer.deserialize(loader, internalClassPath);
        if (classFile == null) {
            throw new IOException(
                "Can not deserialize '" + internalClassPath + "'.");
        }

        // 2) Analyse du byte code
        ReferenceMap referenceMap = new ReferenceMap();
        ClassFileAnalyzer.analyze(referenceMap, classFile);

        // 3) Creation de la liste des references pour generer la liste des
        //    "import"
        ReferenceAnalyzer.analyze(referenceMap, classFile);

        // 4) Mise en page du code source
        List<LayoutBlock> layoutBlockList = new ArrayList<>(1024);
        int maxLineNumber = placement == null
            ? ClassFileLayouter.layout(preferences, referenceMap, classFile, layoutBlockList)
            : ClassFileLayouter.layout(preferences, referenceMap, classFile, layoutBlockList, placement);

//System.out.println("layoutBlockList.size = " + layoutBlockList.size());

        return new Layout(placement, referenceMap, classFile, maxLineNumber, layoutBlockList);
    }

    private record Layout(
            ClassFileLayouter.FieldPlacement placement, ReferenceMap referenceMap, ClassFile classFile,
            int maxLineNumber, List<LayoutBlock> layoutBlockList)
    {
        void write(Loader loader, Printer printer)
        {
            // 5) Ecriture du code source
            ClassFileWriter.write(
                loader, printer, referenceMap, maxLineNumber,
                classFile.getMajorVersion(), classFile.getMinorVersion(),
                layoutBlockList);
        }
    }
}
