package jd.core.test;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.FileImageData;
import net.sourceforge.plantuml.TitledDiagram;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.file.SuggestedFile;
import net.sourceforge.plantuml.klimt.Fashion;
import net.sourceforge.plantuml.klimt.color.HColors;
import net.sourceforge.plantuml.klimt.geom.XPoint2D;
import net.sourceforge.plantuml.klimt.shape.TextBlock;
import net.sourceforge.plantuml.png.PngSplitter;
import net.sourceforge.plantuml.project.GanttDiagram;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.skin.SplitParam;
import net.sourceforge.plantuml.svek.Boundary;

public class ReplaceInstruction {

    double size = 60;

    /*
     * POSTINC
     */
    String normalizeSpace(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        int size = str.length();
        char[] newChars = new char[size];
        int count = 0;
        int whitespacesCount = 0;
        boolean startWhitespaces = true;
        for (int i = 0; i < size; i++) {
            char actualChar = str.charAt(i);
            boolean isWhitespace = Character.isWhitespace(actualChar);
            if (isWhitespace) {
                if (whitespacesCount == 0 && !startWhitespaces) {
                    newChars[count] = StringUtils.SPACE.charAt(0);
					count++;
                }
                whitespacesCount++;
            } else {
                startWhitespaces = false;
                newChars[count] = (actualChar == 160 ? 32 : actualChar);
				count++;
                whitespacesCount = 0;
            }
        }
        if (startWhitespaces) {
            return StringUtils.EMPTY;
        }
        return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0)).trim();
    }

    /*
     * CHECKCAST
     */
    String toString(Object obj, Supplier<String> supplier) {
        return obj == null ? supplier == null ? null : supplier.get() : obj.toString();
    }

    /*
     * IMPLICITCONVERT
     */
    XPoint2D centerHexa(int i, int j) {
        double width = getWidth();
        double x = width * i + (j % 2 == 0 ? 0 : width / 2);
        double y = size * j * 1.5;
        return new XPoint2D(x, y);
    }

    double getWidth() {
        return Math.sqrt(3) / 2 * 2 * size;
    }

    /*
     * INVOKENEW
     */
    TextBlock getDrawing(Fashion symbolContext) {
        return new Boundary(symbolContext.withDeltaShadow(symbolContext.isShadowing() ? 4.0 : 0.0));
    }

    /*
     * NEWARRAY
     */
    boolean[] addFirst(boolean[] array, boolean element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    float[] addFirst(float[] array, float element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    double[] addFirst(double[] array, double element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    byte[] addFirst(byte[] array, byte element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    short[] addFirst(short[] array, short element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    long[] addFirst(long[] array, long element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    /*
     * INSTANCEOF
     */
    List<FileImageData> splitPng(TitledDiagram diagram, SuggestedFile pngFile, ImageData imageData,
            FileFormatOption fileFormatOption) throws IOException {

        List<SFile> files = new PngSplitter(fileFormatOption.getColorMapper(), pngFile,
                diagram.getSplitPagesHorizontal(), diagram.getSplitPagesVertical(),
                fileFormatOption.isWithMetadata() ? diagram.getMetadata() : null, diagram.getSkinParam().getDpi(),
                diagram instanceof GanttDiagram ? new SplitParam(HColors.BLACK, null, 5)
                        : diagram.getSkinParam().getSplitParam()).getFiles();

        List<FileImageData> result = new ArrayList<>();
        for (SFile f : files) {
			result.add(new FileImageData(f, imageData));
		}

        return result;
    }

    /*
     * ARRAYLENGTH
     */
    <E extends Enum<E>> void checkBitVectorable(Class<E> enumClass, E[] constants) {
        Validate.isTrue(constants.length <= Long.SIZE, "Cannot store %s %s values in %s bits",
            constants.length, enumClass.getSimpleName(), Long.SIZE);
    }
}
