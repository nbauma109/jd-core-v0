package jd.core.model.classfile;

import org.apache.bcel.classfile.AnnotationEntry;

import java.util.List;

public record RecordComponent(List<AnnotationEntry> annotations, String name, String signature) {
}
