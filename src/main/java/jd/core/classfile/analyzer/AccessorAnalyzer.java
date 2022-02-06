package jd.core.classfile.analyzer;

import java.util.List;

import jd.core.Constants;
import jd.core.classfile.ClassFile;
import jd.core.classfile.ConstantPool;
import jd.core.classfile.Method;
import jd.core.classfile.accessor.GetFieldAccessor;
import jd.core.classfile.accessor.GetStaticAccessor;
import jd.core.classfile.accessor.InvokeMethodAccessor;
import jd.core.classfile.accessor.PutFieldAccessor;
import jd.core.classfile.accessor.PutStaticAccessor;
import jd.core.classfile.constant.ConstantFieldref;
import jd.core.classfile.constant.ConstantMethodref;
import jd.core.classfile.constant.ConstantNameAndType;
import jd.core.instruction.bytecode.ByteCodeConstants;
import jd.core.instruction.bytecode.instruction.ALoad;
import jd.core.instruction.bytecode.instruction.GetField;
import jd.core.instruction.bytecode.instruction.GetStatic;
import jd.core.instruction.bytecode.instruction.Instruction;
import jd.core.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.instruction.bytecode.instruction.PutField;
import jd.core.instruction.bytecode.instruction.PutStatic;
import jd.core.instruction.bytecode.instruction.ReturnInstruction;

/*
 * Recherche des accesseurs
 */
public class AccessorAnalyzer 
{
	public static void Analyze(ClassFile classFile, Method method)
	{
		// Recherche des accesseurs de champs statiques
		//   static AuthenticatedSubject access$000()
		if (SearchGetStaticAccessor(classFile, method) == true)
			return;
		
		// Recherche des accesseurs de champs statiques
		//   static void access$0(int)
		if (SearchPutStaticAccessor(classFile, method) == true)
			return;
		
		// Recherche des accesseurs de champs
		//   static int access$1(TestInnerClass)
		if (SearchGetFieldAccessor(classFile, method) == true)
			return;
		
		// Recherche des accesseurs de champs
		//   static void access$0(TestInnerClass, int)
		if (SearchPutFieldAccessor(classFile, method) == true)
			return;
		
		// Recherche des accesseurs de methodes
		//   static void access$100(EntitlementFunctionLibrary, EvaluationCtx, URI, Bag, Bag[])
		SearchInvokeMethodAccessor(classFile, method);
	}
	
	/* Recherche des accesseurs de champs statiques:
	 *   static AuthenticatedSubject access$000()
     *   {
     *     Byte code:
     *       getstatic 3	com/bea/security/providers/xacml/entitlement/function/EntitlementFunctionLibrary:kernelId	Lweblogic/security/acl/internal/AuthenticatedSubject;
     *       areturn
     *   }
	 */
	private static boolean SearchGetStaticAccessor(
		ClassFile classFile, Method method)
	{
		List<Instruction> list = method.getInstructions();
		if (list.size() != 1)
			return false;

		Instruction instruction = list.get(0);		
		if (instruction.opcode != ByteCodeConstants.XRETURN)
			return false;
		
		instruction = ((ReturnInstruction)instruction).valueref;
		if (instruction.opcode != ByteCodeConstants.GETSTATIC)
			return false;
		
		ConstantPool constants = classFile.getConstantPool();		
		ConstantFieldref cfr = constants.getConstantFieldref(
			((GetStatic)instruction).index);
		
		if (cfr.class_index != classFile.getThisClassIndex())
			return false;
		
		String methodDescriptor = 
			constants.getConstantUtf8(method.descriptor_index);		
		if (methodDescriptor.charAt(1) != ')')
			return false;
		
		String methodName = constants.getConstantUtf8(method.name_index);
			
		ConstantNameAndType cnat = constants.getConstantNameAndType(
			cfr.name_and_type_index);
		
		String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);		
		String fieldName = constants.getConstantUtf8(cnat.name_index);		
		
		// Trouve ! Ajout de l'accesseur.
		classFile.addAccessor(methodName, methodDescriptor, 
			new GetStaticAccessor(		
				Constants.ACCESSOR_GETSTATIC, 
				classFile.getThisClassName(), fieldName, fieldDescriptor));
		
		return true;
	}
	
	/* Recherche des accesseurs de champs statiques:
     *   static void access$0(int)
     *   {
     *     Byte code:
     *       iload_0
     *       putstatic 11 basic/data/TestInnerClass:test0	I
     *       return
     *   }
	 */	
	private static boolean SearchPutStaticAccessor(
		ClassFile classFile, Method method)
	{
		List<Instruction> list = method.getInstructions();
		if (list.size() != 2)
			return false;
		
		if (list.get(1).opcode != ByteCodeConstants.RETURN)
			return false;
		
		Instruction instruction = list.get(0);
		if (instruction.opcode != ByteCodeConstants.PUTSTATIC)
			return false;
		
		ConstantPool constants = classFile.getConstantPool();		
		ConstantFieldref cfr = constants.getConstantFieldref(
			((PutStatic)instruction).index);
		
		if (cfr.class_index != classFile.getThisClassIndex())
			return false;
		
		String methodDescriptor = 
			constants.getConstantUtf8(method.descriptor_index);		
		if (methodDescriptor.charAt(1) == ')')
			return false;
		if (SignatureAnalyzer.GetParameterSignatureCount(methodDescriptor) != 1)
			return false;
		
		String methodName = constants.getConstantUtf8(method.name_index);
			
		ConstantNameAndType cnat = constants.getConstantNameAndType(
			cfr.name_and_type_index);

		String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);		
		String fieldName = constants.getConstantUtf8(cnat.name_index);		
		
		// Trouve ! Ajout de l'accesseur.
		classFile.addAccessor(methodName, methodDescriptor, 
			new PutStaticAccessor(		
				Constants.ACCESSOR_PUTSTATIC, 
				classFile.getThisClassName(), fieldName, fieldDescriptor));
		
		return true;
	}

	/* Recherche des accesseurs de champs:
     *   static int access$1(TestInnerClass)
     *   {
     *     Byte code:
     *       aload_0
     *       getfield 12 basic/data/TestInnerClass:test	I
     *       ireturn
     *   }
	 */
	private static boolean SearchGetFieldAccessor(
		ClassFile classFile, Method method)
	{
		List<Instruction> list = method.getInstructions();
		if (list.size() != 1)
			return false;

		Instruction instruction = list.get(0);		
		if (instruction.opcode != ByteCodeConstants.XRETURN)
			return false;
		
		instruction = ((ReturnInstruction)instruction).valueref;
		if (instruction.opcode != ByteCodeConstants.GETFIELD)
			return false;
		
		ConstantPool constants = classFile.getConstantPool();		
		ConstantFieldref cfr = constants.getConstantFieldref(
			((GetField)instruction).index);
		
		if (cfr.class_index != classFile.getThisClassIndex())
			return false;
		
		String methodDescriptor = 
			constants.getConstantUtf8(method.descriptor_index);		
		if (methodDescriptor.charAt(1) == ')')
			return false;
		if (SignatureAnalyzer.GetParameterSignatureCount(methodDescriptor) != 1)
			return false;

		String methodName = constants.getConstantUtf8(method.name_index);
			
		ConstantNameAndType cnat = constants.getConstantNameAndType(
			cfr.name_and_type_index);
		
		String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);		
		String fieldName = constants.getConstantUtf8(cnat.name_index);		
		
		// Trouve ! Ajout de l'accesseur.
		classFile.addAccessor(methodName, methodDescriptor, 
			new GetFieldAccessor(		
				Constants.ACCESSOR_GETFIELD, 
				classFile.getThisClassName(), fieldName, fieldDescriptor));
		
		return true;
	}
	
	/* Recherche des accesseurs de champs:
	 *   static void access$0(TestInnerClass, int)
     *   {
     *     Byte code:
     *       aload_0
     *       iload_1
     *       putfield 13 basic/data/TestInnerClass:test	I
     *       return
     *   }
	 */
	private static boolean SearchPutFieldAccessor(
		ClassFile classFile, Method method)
	{
		List<Instruction> list = method.getInstructions();
		if (list.size() != 2)
			return false;
		
		if (list.get(1).opcode != ByteCodeConstants.RETURN)
			return false;

		Instruction instruction = list.get(0);
		if (instruction.opcode != ByteCodeConstants.PUTFIELD)
			return false;
		
		PutField pf = (PutField)instruction;
		ConstantPool constants = classFile.getConstantPool();	
		ConstantFieldref cfr = constants.getConstantFieldref(pf.index);
		
		if (cfr.class_index != classFile.getThisClassIndex())
			return false;
		
		String methodDescriptor = 
			constants.getConstantUtf8(method.descriptor_index);
		if (methodDescriptor.charAt(1) == ')')
			return false;
		if (SignatureAnalyzer.GetParameterSignatureCount(methodDescriptor) != 2)
			return false;
		
		ConstantNameAndType cnat = constants.getConstantNameAndType(
				cfr.name_and_type_index);
		
		String methodName = constants.getConstantUtf8(method.name_index);	
		String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);		
		String fieldName = constants.getConstantUtf8(cnat.name_index);	
		
		// Trouve ! Ajout de l'accesseur.
		classFile.addAccessor(methodName, methodDescriptor, 
			new PutFieldAccessor(
				Constants.ACCESSOR_PUTFIELD,
				classFile.getThisClassName(), fieldName, fieldDescriptor));
		
		return false;
	}
	
	/* Recherche des accesseurs de methodes:
	 *     static void access$100(EntitlementFunctionLibrary, EvaluationCtx, URI, Bag, Bag[])
     *     {
     *       Byte code:
     *         aload_0
     *         aload_1
     *         aload_2
     *         aload_3
     *         aload 4
     *         invokevirtual 2	com/bea/security/providers/xacml/entitlement/function/EntitlementFunctionLibrary:debugEval	(Lcom/bea/security/xacml/EvaluationCtx;Ljava/net/URI;Lcom/bea/common/security/xacml/attr/Bag;[Lcom/bea/common/security/xacml/attr/Bag;)V
     *         return
     *     }
	 */
	private static boolean SearchInvokeMethodAccessor(
		ClassFile classFile, Method method)
	{
		List<Instruction> list = method.getInstructions();
		Instruction instruction;
		
		switch(list.size())
		{
		case 1:
			instruction = list.get(0);	
			if (instruction.opcode != ByteCodeConstants.XRETURN)
				return false;
			instruction = ((ReturnInstruction)instruction).valueref;			
			break;
		case 2:
			instruction = list.get(1);
			if (instruction.opcode != ByteCodeConstants.RETURN)
				return false;
			instruction = list.get(0);			
			break;
		default:
			return false;
		}
		
		switch (instruction.opcode)
		{
		case ByteCodeConstants.INVOKEINTERFACE:
		case ByteCodeConstants.INVOKEVIRTUAL:
		case ByteCodeConstants.INVOKESPECIAL:
			break;
		default:
			return false;
		}
		
		InvokeNoStaticInstruction insi = 
			(InvokeNoStaticInstruction)instruction;
		
		if ((insi.objectref.opcode != ByteCodeConstants.ALOAD) || 
			(((ALoad)insi.objectref).index != 0))		
			return false;
		
		ConstantPool constants = classFile.getConstantPool();	

		String methodName = constants.getConstantUtf8(method.name_index);	
		String methodDescriptor = 
			constants.getConstantUtf8(method.descriptor_index);
			
		ConstantMethodref cmr = constants.getConstantMethodref(insi.index);
		ConstantNameAndType cnat = constants.getConstantNameAndType(
			cmr.name_and_type_index);
		
		String targetMethodName = constants.getConstantUtf8(cnat.name_index);	
		String targetMethodDescriptor = 
			constants.getConstantUtf8(cnat.descriptor_index);		

		// Trouve ! Ajout de l'accesseur.
		classFile.addAccessor(methodName, methodDescriptor, 
			new InvokeMethodAccessor(		
				Constants.ACCESSOR_INVOKEMETHOD, classFile.getThisClassName(), 
				insi.opcode, targetMethodName, targetMethodDescriptor,
				cmr.getListOfParameterSignatures(),
				cmr.getReturnedSignature()));
		
		return true;
	}
}
