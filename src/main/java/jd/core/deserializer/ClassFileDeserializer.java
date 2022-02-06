package jd.core.deserializer;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.Field;
import jd.core.classfile.Method;
import jd.core.classfile.attribute.Attribute;
import jd.core.classfile.attribute.AttributeInnerClasses;
import jd.core.classfile.attribute.InnerClass;
import jd.core.classfile.constant.Constant;
import jd.core.classfile.constant.ConstantClass;
import jd.core.classfile.constant.ConstantDouble;
import jd.core.classfile.constant.ConstantFieldref;
import jd.core.classfile.constant.ConstantFloat;
import jd.core.classfile.constant.ConstantInteger;
import jd.core.classfile.constant.ConstantInterfaceMethodref;
import jd.core.classfile.constant.ConstantLong;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.classfile.constant.ConstantNameAndType;
import jd.core.classfile.constant.ConstantString;
import jd.core.classfile.constant.ConstantUtf8;
import jd.core.exception.ClassFormatException;
import jd.core.loader.Loader;
import jd.core.loader.LoaderException;



public class ClassFileDeserializer 
{
	public static ClassFile Deserialize(Loader loader, String internalPath) 
		throws LoaderException
	{
		ClassFile classFile = LoadSingleClass(loader, internalPath);
		if (classFile == null)
			return null;
		
		AttributeInnerClasses aics = classFile.getAttributeInnerClasses();
		if (aics == null)
			return classFile;
		
		String internalPathPrefix = 
			internalPath.substring(
				0, internalPath.length() - Constants.CLASS_FILE_SUFFIX.length());
		String innerInternalClassNamePrefix = 
			internalPathPrefix + Constants.INTERNAL_INNER_SEPARATOR;	
		ConstantPool constants = classFile.getConstantPool();
		ArrayList<ClassFile> innerClassFiles = null;

		InnerClass[] cs = aics.classes;
		int length = cs.length;
		
		for (int i=0; i<length; i++)
		{
	    	String innerInternalClassName = 
	    		constants.getConstantClassName(cs[i].inner_class_index);
	    	
			if (! innerInternalClassName.startsWith(innerInternalClassNamePrefix))
				continue;			
			int offsetInternalInnerSeparator = innerInternalClassName.indexOf(
				Constants.INTERNAL_INNER_SEPARATOR, 
				innerInternalClassNamePrefix.length());
			if (offsetInternalInnerSeparator != -1)
			{
				String tmpInnerInternalClassName = 
					innerInternalClassName.substring(0, offsetInternalInnerSeparator) + 
					Constants.CLASS_FILE_SUFFIX;
				if (loader.canLoad(tmpInnerInternalClassName))
					// 'innerInternalClassName' is not a direct inner classe.
					continue;	
			}
			
			try
			{
				ClassFile innerClassFile = 
					Deserialize(loader, innerInternalClassName + 
					Constants.CLASS_FILE_SUFFIX);
				
				if (innerClassFile != null)
				{
					// Alter inner class access flag
					innerClassFile.setAccessFlags(cs[i].inner_access_flags);
					// Setup outer class reference
					innerClassFile.setOuterClass(classFile);
		
					if (innerClassFiles == null)
						innerClassFiles = new ArrayList<ClassFile>();
						
					innerClassFiles.add(innerClassFile);
				}
			}
			catch (LoaderException e)
			{}
		}
		
		// Add inner classes
		if (innerClassFiles != null)
		{
			ClassFile[] array = new ClassFile[innerClassFiles.size()];
			classFile.setInnerClassFiles(innerClassFiles.toArray(array));
		}
		
		return classFile;
	}

	private static ClassFile LoadSingleClass(
			Loader loader, String internalClassName) 
		throws LoaderException
	{
		DataInputStream dis = null;
		ClassFile classFile = null;

		try 
		{
			dis = loader.load(internalClassName);
			if (dis != null)
				classFile = Deserialize(dis);
		}
		catch (IOException e) 
		{
			classFile = null;
			e.printStackTrace();
		}
		finally
		{
			if (dis != null)
				try { dis.close(); } catch (IOException e) { }
		}
		
		return classFile;
	}
	
	private static ClassFile Deserialize(DataInput di)
		throws IOException
	{
		CheckMagic(di);
	
		int minor_version = di.readUnsignedShort();
		int major_version = di.readUnsignedShort();
		
		Constant[] constants = DeserializeConstants(di);
		ConstantPool constantPool = new ConstantPool(constants);
		
		int access_flags = di.readUnsignedShort();
		int this_class = di.readUnsignedShort();
		int super_class = di.readUnsignedShort();
		
		int[] interfaces = DeserializeInterfaces(di);
			
		Field[] fieldInfos = DeserializeFields(di, constantPool);
	
		Method[] methodInfos = DeserializeMethods(di, constantPool);
	
		Attribute[] attributeInfos = 
			AttributeDeserializer.Deserialize(di, constantPool);
					
		return new ClassFile(
				minor_version, major_version,
				constantPool,
				access_flags, this_class, super_class,
				interfaces,
				fieldInfos,
				methodInfos,
				attributeInfos
		);
	}
	
	private static Constant[] DeserializeConstants(DataInput di)
		throws IOException
	{
		int count = di.readUnsignedShort();
		if (count == 0)
			return null;
		
		Constant[] constants = new Constant[count];
		
		for (int i=1; i<count; i++)
		{
			byte tag = di.readByte();
			
			switch (tag)
			{
			case Constants.CONSTANT_Class:
				constants[i] = new ConstantClass(tag, di.readUnsignedShort());
				break;
			case Constants.CONSTANT_Fieldref:
				constants[i] = new ConstantFieldref(tag, 
						                            di.readUnsignedShort(),
	                                                di.readUnsignedShort());
				break;
			case Constants.CONSTANT_Methodref:
				constants[i] = new ConstantMethodref(tag, 
						                             di.readUnsignedShort(),
	                                                 di.readUnsignedShort());
				break;
			case Constants.CONSTANT_InterfaceMethodref:
				constants[i] = new ConstantInterfaceMethodref(
													   tag, 
						                               di.readUnsignedShort(),
	                                                   di.readUnsignedShort());
				break;
			case Constants.CONSTANT_String:
				constants[i] = new ConstantString(tag, di.readUnsignedShort());
				break;
			case Constants.CONSTANT_Integer:
				constants[i] = new ConstantInteger(tag, di.readInt());
				break;
			case Constants.CONSTANT_Float:
				constants[i] = new ConstantFloat(tag, di.readFloat());
				break;
			case Constants.CONSTANT_Long:
				constants[i++] = new ConstantLong(tag, di.readLong());
				break;
			case Constants.CONSTANT_Double:
				constants[i++] = new ConstantDouble(tag, di.readDouble());
				break;
			case Constants.CONSTANT_NameAndType:
				constants[i] = new ConstantNameAndType(tag, 
						                               di.readUnsignedShort(), 
	                                                   di.readUnsignedShort());
				break;
			case Constants.CONSTANT_Utf8:			
				constants[i] = new ConstantUtf8(tag, di.readUTF());
				break;
			default:
				throw new ClassFormatException("Invalid constant pool entry");
			}
		}
		
		return constants;
	}
	
	private static int[] DeserializeInterfaces(DataInput di)
		throws IOException
	{
		int count = di.readUnsignedShort();
		if (count == 0)
			return null;
		
		int[] interfaces = new int[count];
		
		for (int i=0; i<count; i++)
			interfaces[i] = di.readUnsignedShort();
	
		return interfaces;
	}
	
	private static Field[] DeserializeFields(
			DataInput di, ConstantPool constantPool)
		throws IOException
	{
		int count = di.readUnsignedShort();
		if (count == 0)
			return null;
		
		Field[] fieldInfos = new Field[count];
		
		for (int i=0; i<count; i++)
			fieldInfos[i] = new Field(
						di.readUnsignedShort(), 
						di.readUnsignedShort(), 
						di.readUnsignedShort(), 
						AttributeDeserializer.Deserialize(di, constantPool));
		
		return fieldInfos;
	}
	
	private static Method[] DeserializeMethods(DataInput di, 
			ConstantPool constants)
		throws IOException
	{
		int count = di.readUnsignedShort();
		if (count == 0)
			return null;
		
		Method[] methodInfos = new Method[count];
		
		for (int i=0; i<count; i++)
			methodInfos[i] = new Method(
						di.readUnsignedShort(), 
						di.readUnsignedShort(), 
						di.readUnsignedShort(), 
						AttributeDeserializer.Deserialize(di, constants));
		
		return methodInfos;
	}
	
	private static void CheckMagic(DataInput di)
		throws IOException
	{
	    int magic = di.readInt();
	
	    if(magic != Constants.MAGIC_NUMBER)
	      throw new ClassFormatException("Invalid Java .class file");
	}
}
