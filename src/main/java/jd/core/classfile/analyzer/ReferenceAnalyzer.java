package jd.core.classfile.analyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.Field;
import jd.core.classfile.LocalVariable;
import jd.core.classfile.LocalVariables;
import jd.core.classfile.Method;
import jd.core.classfile.attribute.Annotation;
import jd.core.classfile.attribute.Attribute;
import jd.core.classfile.attribute.AttributeRuntimeInvisibleAnnotations;
import jd.core.classfile.attribute.AttributeRuntimeInvisibleParameterAnnotations;
import jd.core.classfile.attribute.AttributeRuntimeVisibleAnnotations;
import jd.core.classfile.attribute.AttributeRuntimeVisibleParameterAnnotations;
import jd.core.classfile.attribute.AttributeSignature;
import jd.core.classfile.attribute.ElementValue;
import jd.core.classfile.attribute.ElementValueAnnotationValue;
import jd.core.classfile.attribute.ElementValueArrayValue;
import jd.core.classfile.attribute.ElementValueClassInfo;
import jd.core.classfile.attribute.ElementValueEnumConstValue;
import jd.core.classfile.attribute.ElementValuePair;
import jd.core.classfile.attribute.ParameterAnnotations;
import jd.core.classfile.visitor.ReferenceVisitor;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.util.Reference;
import jd.core.util.ReferenceMap;



public class ReferenceAnalyzer 
{
	public static void Analyze(
		ReferenceMap referenceMap, ClassFile classFile)
	{
		CountReferences(referenceMap, classFile);		
		ReduceReferences(referenceMap, classFile);
	}
	
	private static void CountReferences(
			ReferenceMap referenceMap, ClassFile classFile)
	{
		// Class
		referenceMap.add(classFile.getThisClassName());
		
		AttributeSignature as = classFile.getAttributeSignature();
		if (as == null)
		{
			// Super class
			if (classFile.getSuperClassIndex() != 0)
				referenceMap.add(classFile.getSuperClassName());
			// Interfaces
			int[] interfaces = classFile.getInterfaces();
			if (interfaces != null)
				for(int i=interfaces.length-1; i>=0; --i) 
				{
					String internalInterfaceName = 
						classFile.getConstantPool().getConstantClassName(interfaces[i]);
					referenceMap.add(internalInterfaceName);
				}
		}
		else
		{
			String signature = 
				classFile.getConstantPool().getConstantUtf8(as.signature_index);
			SignatureAnalyzer.AnalyzeClassSignature(referenceMap, signature);
		}
		
		// Class annotations
		CountReferencesInAttributes(
			referenceMap, classFile.getConstantPool(), classFile.getAttributes());
		
		// Inner classes
		ClassFile[] innerClassFiles = classFile.getInnerClassFiles();	
		if (innerClassFiles != null)
			for (int i=innerClassFiles.length-1; i>=0; --i)
				CountReferences(referenceMap, innerClassFiles[i]);
		
		ReferenceVisitor visitor = 
			new ReferenceVisitor(classFile.getConstantPool(), referenceMap);
		
		// Fields
		CountReferencesInFields(referenceMap, visitor, classFile);
		
		// Methods
		CountReferencesInMethods(referenceMap, visitor, classFile);
	}
	
	private static void CountReferencesInAttributes(
			ReferenceMap referenceMap, ConstantPool constants, 
			Attribute[] attributes)
	{
		Annotation[] annotations;
		
		if (attributes != null)
			for (int i=attributes.length-1; i>=0; --i)
				switch (attributes[i].tag)
				{
				case Constants.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS:
					{
						annotations = 
							((AttributeRuntimeInvisibleAnnotations)attributes[i])
							.annotations;
						for (int j=annotations.length-1; j>=0; --j)
							CountAnnotationReference(referenceMap, constants, annotations[j]);
					}
					break;

				case Constants.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
					{
						ParameterAnnotations[] parameterAnnotations = 
							((AttributeRuntimeInvisibleParameterAnnotations)
									attributes[i]).parameter_annotations;
						CountParameterAnnotationsReference(
								referenceMap, constants, parameterAnnotations);
					}
					break;
					
				case Constants.ATTR_RUNTIME_VISIBLE_ANNOTATIONS:
					{
						annotations = 
							((AttributeRuntimeVisibleAnnotations)attributes[i])
							.annotations;
						for (int j=annotations.length-1; j>=0; --j)
							CountAnnotationReference(referenceMap, constants, annotations[j]);
					}
					break;

				case Constants.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
					{
						ParameterAnnotations[] parameterAnnotations = 
							((AttributeRuntimeVisibleParameterAnnotations)
									attributes[i]).parameter_annotations;
						CountParameterAnnotationsReference(
							referenceMap, constants, parameterAnnotations);
					}
					break;
				}
	}

	private static void CountAnnotationReference(
			ReferenceMap referenceMap, ConstantPool constants, 
			Annotation annotation)
	{
		String typeName = constants.getConstantUtf8(annotation.type_index);
		SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, typeName);
		
		ElementValuePair[] evps = annotation.elementValuePairs;
		if (evps != null)
			for (int j=evps.length-1; j>=0; --j)
				CountElementValue(referenceMap, constants, evps[j].element_value);
	}
	
	private static void CountParameterAnnotationsReference(
			ReferenceMap referenceMap, ConstantPool constants, 
			ParameterAnnotations[] parameterAnnotations)
	{
		if (parameterAnnotations != null)
		{
			for (int i=parameterAnnotations.length-1; i>=0; --i)
			{
				Annotation[] annotations = parameterAnnotations[i].annotations;
				if (annotations != null)
				{
					for (int j=annotations.length-1; j>=0; --j)
						CountAnnotationReference(
							referenceMap, constants, annotations[j]);
				}
			}
		}
	}
	
	private static void CountElementValue(
			ReferenceMap referenceMap, ConstantPool constants, ElementValue ev)
	{
		String signature;
		ElementValueClassInfo evci;
		
		switch (ev.tag)
		{
		case Constants.EV_CLASS_INFO:
			{
				evci = (ElementValueClassInfo)ev;
				signature = constants.getConstantUtf8(evci.class_info_index);
				SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
			}
			break;
		case Constants.EV_ANNOTATION_VALUE:
			{
				ElementValueAnnotationValue evanv = (ElementValueAnnotationValue)ev;
				CountAnnotationReference(
						referenceMap, constants, evanv.annotation_value);
			}
			break;
		case Constants.EV_ARRAY_VALUE:
			{
				ElementValueArrayValue evarv = (ElementValueArrayValue)ev;
				ElementValue[] values = evarv.values;
	
				if (values != null)
					for (int i=values.length-1; i>=0; --i)
						if (values[i].tag == Constants.EV_CLASS_INFO)
						{
							evci = (ElementValueClassInfo)values[i];
							signature = 
								constants.getConstantUtf8(evci.class_info_index);			
							SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
						}
			}
			break;
		case Constants.EV_ENUM_CONST_VALUE:
			{
				ElementValueEnumConstValue evecv = (ElementValueEnumConstValue)ev;
				signature = constants.getConstantUtf8(evecv.type_name_index);
				SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
			}
			break;
		}
	}

	private static void CountReferencesInFields(
			ReferenceMap referenceMap, ReferenceVisitor visitor, 
			ClassFile classFile)
	{
		Field[] fields = classFile.getFields();
		
		if (fields != null)
		    for (int i=fields.length-1; i>=0; --i)
			{
				Field field = fields[i];
				
		    	if ((field.access_flags & Constants.ACC_SYNTHETIC) != 0)
		    		continue;
		    	
		    	CountReferencesInAttributes(
		    			referenceMap, classFile.getConstantPool(), field.getAttributes());
		    	
				AttributeSignature as = field.getAttributeSignature();
				String signature = classFile.getConstantPool().getConstantUtf8(
					(as==null) ? field.descriptor_index : as.signature_index);
				SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
				
				if (field.getValueAndLocalVariables() != null)
					visitor.visit(field.getValueAndLocalVariables().getValue());
		    }
	}
	
	private static void CountReferencesInMethods(
			ReferenceMap referenceMap, ReferenceVisitor visitor, 
			ClassFile classFile)
	{
		Method[] methods = classFile.getMethods();
		ConstantPool constants = classFile.getConstantPool();
			
		if (methods == null)
			return;
		
	    for (int i=methods.length-1; i>=0; --i)
		{
	    	Method method = methods[i];
	    	
	    	if (((method.access_flags & 
	    		 (Constants.ACC_SYNTHETIC|Constants.ACC_BRIDGE)) != 0) ||
	    		method.containsError())
	    		continue;
	    	
	    	CountReferencesInAttributes(
	    			referenceMap, classFile.getConstantPool(), method.getAttributes());
			
	    	// Signature
			AttributeSignature as = method.getAttributeSignature();
			String signature = constants.getConstantUtf8(
					(as==null) ? method.descriptor_index : as.signature_index);
			SignatureAnalyzer.AnalyzeMethodSignature(referenceMap, signature);
			
	    	// Exceptions
			int[] exceptionIndexes = method.getExceptionIndexes();
			if (exceptionIndexes != null)
				for (int j=exceptionIndexes.length-1; j>=0; --j)
					referenceMap.add(
						constants.getConstantClassName(exceptionIndexes[j]));
			
			// Local variables
			LocalVariables localVariables = method.getLocalVariables();
			if (localVariables != null)
				CountReferencesInLocalVariables(
						referenceMap, constants, localVariables);		
			
			// Code
			CountReferencesInCode(visitor, method);
		}
	}
	
	private static void CountReferencesInLocalVariables(
			ReferenceMap referenceMap, ConstantPool constants, 
			LocalVariables localVariables)
	{
		if (localVariables != null)
		{
			for (int i=localVariables.size()-1; i>=0; --i)
			{
				LocalVariable lv = localVariables.getLocalVariableAt(i);
				
				if ((lv != null) && (lv.signature_index > 0))
				{
					String signature = 
						constants.getConstantUtf8(lv.signature_index);
					SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
				}
			}
		}
	}

	private static void CountReferencesInCode(
		ReferenceVisitor visitor, Method method)
	{
		List<Instruction> instructions = method.getFastNodes();
		
		if (instructions != null)
		{
			for (int i=instructions.size()-1; i>=0; --i)
				visitor.visit(instructions.get(i));
		}
	}

	private static void ReduceReferences(
		ReferenceMap referenceMap, ClassFile classFile)
	{		
		HashMap<String, Boolean> multipleInternalClassName = 
			new HashMap<String, Boolean>();
		
		Iterator<Reference> iterator = referenceMap.values().iterator();
		while (iterator.hasNext())
		{
			Reference reference = iterator.next();
			String internalName = reference.getInternalName();
			int index = 
				internalName.lastIndexOf(Constants.INTERNAL_PACKAGE_SEPARATOR);
			String internalClassName = 
				(index != -1) ? internalName.substring(index+1) : internalName;
				
			if (multipleInternalClassName.containsKey(internalClassName))
				multipleInternalClassName.put(internalClassName, Boolean.TRUE);
			else
				multipleInternalClassName.put(internalClassName, Boolean.FALSE);				
		}
		
		iterator = referenceMap.values().iterator();
		while (iterator.hasNext())
		{
			Reference reference = iterator.next();
			String internalName = reference.getInternalName();
			int index = 
				internalName.lastIndexOf(Constants.INTERNAL_PACKAGE_SEPARATOR);
			String internalPackageName;
			String internalClassName;
			
			if (index != -1)
			{
				internalPackageName = internalName.substring(0, index);
				internalClassName = internalName.substring(index+1);
			}
			else
			{
				internalPackageName = "";
				internalClassName = internalName;
			}
			
			String internalPackageName_className = 
				classFile.getInternalPackageName() + 
				Constants.INTERNAL_PACKAGE_SEPARATOR + internalClassName;
				
			if (!internalPackageName.equals(classFile.getInternalPackageName()) && 
				multipleInternalClassName.get(internalClassName).booleanValue())
			{
				// Remove references with same name and different packages
				iterator.remove();
			}
			else if (referenceMap.contains(internalPackageName_className))
			{
				// Remove references with a name of same package of current class
				iterator.remove();
			}
		}		
	}
}