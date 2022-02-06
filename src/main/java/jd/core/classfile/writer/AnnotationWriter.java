package jd.core.classfile.writer;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.attribute.Annotation;
import jd.core.classfile.attribute.Attribute;
import jd.core.classfile.attribute.AttributeRuntimeInvisibleAnnotations;
import jd.core.classfile.attribute.AttributeRuntimeVisibleAnnotations;
import jd.core.classfile.attribute.ElementValue;
import jd.core.classfile.attribute.ElementValueAnnotationValue;
import jd.core.classfile.attribute.ElementValueArrayValue;
import jd.core.classfile.attribute.ElementValueClassInfo;
import jd.core.classfile.attribute.ElementValueEnumConstValue;
import jd.core.classfile.attribute.ElementValuePair;
import jd.core.classfile.attribute.ElementValuePrimitiveType;
import jd.core.classfile.attribute.ParameterAnnotations;
import jd.core.printer.Printer;
import jd.core.util.ReferenceMap;


public class AnnotationWriter 
{
	public static void WriteAttributeAnnotations(
		Printer spw, ReferenceMap referenceMap, 
		ClassFile classFile, Attribute[] attributes)
	{
		if (attributes == null)
			return;
		
		for (int i=0; i<attributes.length; i++)
		{
			Annotation[] annotations;
			
			switch(attributes[i].tag)
			{
			case Constants.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS:
				annotations = 
					((AttributeRuntimeInvisibleAnnotations)attributes[i])
					.annotations;
				WriteAnnotations(spw, referenceMap, classFile, annotations);
				break;

			case Constants.ATTR_RUNTIME_VISIBLE_ANNOTATIONS:
				annotations = 
					((AttributeRuntimeVisibleAnnotations)attributes[i])
					.annotations;
				WriteAnnotations(spw, referenceMap, classFile, annotations);
			}
		}
	}

	private static void WriteAnnotations(
			Printer spw, ReferenceMap referenceMap, 
			ClassFile classFile, Annotation[] annotations)
	{
		if (annotations == null)
			return;
		
		for (int i=0; i<annotations.length; i++)
		{
			spw.startAnnotation();
			WriteAnnotation(spw, referenceMap, classFile, annotations[i]);
			spw.endAnnotation();
		}
	}

	public static void WriteParameterAnnotation(
			Printer spw, ReferenceMap referenceMap, 
			ClassFile classFile, ParameterAnnotations parameterAnnotation)
	{
		if (parameterAnnotation == null)
			return;
		
		Annotation[] annotations = parameterAnnotation.annotations;

		if (annotations == null)
			return;
		
		for (int i=0; i<annotations.length; i++)
		{
			WriteAnnotation(spw, referenceMap, classFile, annotations[i]);
			spw.print(Printer.UNKNOWN_LINE_NUMBER, ' ');
		}
	}
	
	private static void WriteAnnotation(
		Printer spw, ReferenceMap referenceMap, 
		ClassFile classFile, Annotation annotation)
	{
		spw.startAnnotationName();
		spw.print(Printer.UNKNOWN_LINE_NUMBER, '@');
		
		String annotationName = 
			classFile.getConstantPool().getConstantUtf8(annotation.type_index);
		SignatureWriter.WriteSimpleSignature(
			spw, Printer.UNKNOWN_LINE_NUMBER, referenceMap, 
			classFile, annotationName);
		spw.endAnnotationName();
		
		ElementValuePair[] evps = annotation.elementValuePairs;
		if (evps != null)
		{
			if (evps.length > 0)
			{			
				spw.print(Printer.UNKNOWN_LINE_NUMBER, '(');
				
				ConstantPool constants = classFile.getConstantPool();
				String name = constants.getConstantUtf8(evps[0].element_name_index);
				
				if ((evps.length > 1) || !"value".equals(name))
				{
					spw.print(Printer.UNKNOWN_LINE_NUMBER, name);
					spw.print(Printer.UNKNOWN_LINE_NUMBER, '=');
				}
				WriteElementValue(
					spw, referenceMap, classFile, evps[0].element_value);
				
				for (int j=1; j<evps.length; j++)
				{
					name = constants.getConstantUtf8(evps[j].element_name_index);

					spw.print(Printer.UNKNOWN_LINE_NUMBER, ", ");
					spw.print(Printer.UNKNOWN_LINE_NUMBER, name);
					spw.print(Printer.UNKNOWN_LINE_NUMBER, '=');
					WriteElementValue(
						spw, referenceMap, classFile, evps[j].element_value);
				}
				
				spw.print(Printer.UNKNOWN_LINE_NUMBER, ')');
			}
		}
	}

	private static void WriteElementValue(
			Printer spw, ReferenceMap referenceMap, 
			ClassFile classFile, ElementValue ev)
	{
		ConstantPool constants = classFile.getConstantPool();
		
		switch (ev.tag)
		{
		case Constants.EV_PRIMITIVE_TYPE:
			ElementValuePrimitiveType evpt = (ElementValuePrimitiveType)ev;
			ElementValuePrimitiveTypeWriter.Write(
				spw, Printer.UNKNOWN_LINE_NUMBER, constants, evpt);
			break;
			
		case Constants.EV_CLASS_INFO:
			ElementValueClassInfo evci = (ElementValueClassInfo)ev;
			String signature = 
				constants.getConstantUtf8(evci.class_info_index);
			SignatureWriter.WriteSimpleSignature(
				spw, Printer.UNKNOWN_LINE_NUMBER, referenceMap, 
				classFile, signature);
			spw.printKeyword(Printer.UNKNOWN_LINE_NUMBER, ".class");
			break;
			
		case Constants.EV_ANNOTATION_VALUE:
			ElementValueAnnotationValue evav = (ElementValueAnnotationValue)ev;
			WriteAnnotation(
				spw, referenceMap, classFile, evav.annotation_value);
			break;
			
		case Constants.EV_ARRAY_VALUE:
			ElementValueArrayValue evarv = (ElementValueArrayValue)ev;
			ElementValue[] values = evarv.values;

			spw.print(Printer.UNKNOWN_LINE_NUMBER, '{');
			if (values.length > 0)
			{
				WriteElementValue(spw, referenceMap, classFile, values[0]);
				for (int i=1; i<values.length; i++)
				{
					spw.print(Printer.UNKNOWN_LINE_NUMBER, ", ");
					WriteElementValue(
						spw, referenceMap, classFile, values[i]);
				}
			}
			spw.print(Printer.UNKNOWN_LINE_NUMBER, '}');
			break;
			
		case Constants.EV_ENUM_CONST_VALUE:
			ElementValueEnumConstValue evecv = (ElementValueEnumConstValue)ev;
			signature = constants.getConstantUtf8(evecv.type_name_index);
			SignatureWriter.WriteSimpleSignature(
				spw, Printer.UNKNOWN_LINE_NUMBER, referenceMap, 
				classFile, signature);
			spw.print(Printer.UNKNOWN_LINE_NUMBER, '.');
			spw.print(
				Printer.UNKNOWN_LINE_NUMBER, 
				constants.getConstantUtf8(evecv.const_name_index));
		}
	}
}