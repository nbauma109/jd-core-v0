package jd.core.test;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.RuntimeInvisibleAnnotations;
import org.apache.bcel.classfile.RuntimeVisibleAnnotations;
import org.apache.bcel.generic.AnnotationEntryGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.util.ClassPath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TryResourcesBCEL {

    public byte[] getBytes(ClassPath cp, String name, String suffix) throws IOException {
        DataInputStream dis = null;
        try (InputStream inputStream = cp.getInputStream(name, suffix)) {
            if (inputStream == null) {
                throw new IOException("Couldn't find: " + name + suffix);
            }
            dis = new DataInputStream(inputStream);
            byte[] bytes = new byte[inputStream.available()];
            dis.readFully(bytes);
            return bytes;
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }

    static Attribute[] getAnnotationAttributes(ConstantPoolGen cp, AnnotationEntryGen[] annotationEntryGens) {
        if (annotationEntryGens.length == 0) {
            return Attribute.EMPTY_ARRAY;
        }

        try {
            int countVisible = 0;
            int countInvisible = 0;

            for (AnnotationEntryGen a : annotationEntryGens) {
                if (a.isRuntimeVisible()) {
                    countVisible++;
                } else {
                    countInvisible++;
                }
            }

            ByteArrayOutputStream rvaBytes = new ByteArrayOutputStream();
            ByteArrayOutputStream riaBytes = new ByteArrayOutputStream();
            try (DataOutputStream rvaDos = new DataOutputStream(rvaBytes); DataOutputStream riaDos = new DataOutputStream(riaBytes)) {

                rvaDos.writeShort(countVisible);
                riaDos.writeShort(countInvisible);

                for (AnnotationEntryGen a : annotationEntryGens) {
                    if (a.isRuntimeVisible()) {
                        a.dump(rvaDos);
                    } else {
                        a.dump(riaDos);
                    }
                }
            }

            byte[] rvaData = rvaBytes.toByteArray();
            byte[] riaData = riaBytes.toByteArray();

            int rvaIndex = -1;
            int riaIndex = -1;

            if (rvaData.length > 2) {
                rvaIndex = cp.addUtf8("RuntimeVisibleAnnotations");
            }
            if (riaData.length > 2) {
                riaIndex = cp.addUtf8("RuntimeInvisibleAnnotations");
            }

            List<Attribute> newAttributes = new ArrayList<>();
            if (rvaData.length > 2) {
                newAttributes
                    .add(new RuntimeVisibleAnnotations(rvaIndex, rvaData.length, new DataInputStream(new ByteArrayInputStream(rvaData)), cp.getConstantPool()));
            }
            if (riaData.length > 2) {
                newAttributes.add(
                    new RuntimeInvisibleAnnotations(riaIndex, riaData.length, new DataInputStream(new ByteArrayInputStream(riaData)), cp.getConstantPool()));
            }

            return newAttributes.toArray(Attribute.EMPTY_ARRAY);
        } catch (IOException e) {
            System.err.println("IOException whilst processing annotations");
            e.printStackTrace();
        }
        return null;
    }
}
