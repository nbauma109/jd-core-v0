package jd.core.process.layouter.visitor;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.layout.block.InstructionLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.layout.block.LayoutBlockConstants;
import jd.core.preferences.Preferences;
import jd.core.process.layouter.ClassFileLayouter;

public class InstructionSplitterVisitor extends BaseInstructionSplitterVisitor 
{
	protected Preferences preferences;
	protected List<LayoutBlock> layoutBlockList;
	protected Method method;
	protected Instruction instruction;
	protected int firstLineNumber;
	protected int offset1;
	
	public InstructionSplitterVisitor() {}

	public void start(
		Preferences preferences, 
		List<LayoutBlock> layoutBlockList, ClassFile classFile, 
		Method method, Instruction instruction)
	{
		super.start(classFile);
		
		this.preferences = preferences;
		this.layoutBlockList = layoutBlockList;
		this.method = method;
		this.instruction = instruction;
		this.firstLineNumber = MinLineNumberVisitor.visit(instruction);
		this.offset1 = 0;
	}
	
	public void end()
	{
		// S'il reste un fragment d'instruction a traiter...
		if ((this.offset1 == 0) || (this.offset1 != this.instruction.offset))
		{
	    	// Add last part of instruction
	    	int lastLineNumber = MaxLineNumberVisitor.visit(instruction);
	    	int preferedLineNumber;
	    	
	    	if ((firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER) &&
				(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER))
			{
				preferedLineNumber = lastLineNumber-firstLineNumber;
			}
			else
			{
				preferedLineNumber = LayoutBlockConstants.UNLIMITED_LINE_COUNT;
			}
	
			layoutBlockList.add(new InstructionLayoutBlock(
				LayoutBlockConstants.INSTRUCTION,
				this.firstLineNumber, lastLineNumber, 
				preferedLineNumber, preferedLineNumber, preferedLineNumber, 
				this.classFile, this.method, this.instruction, 
				this.offset1, this.instruction.offset));
		}
	}
	
	public void visitAnonymousNewInvoke(
		Instruction parent, InvokeNew in, ClassFile innerClassFile) 
	{
		// Add a new part of instruction
		int lastLineNumber = MaxLineNumberVisitor.visit(in);
    	int preferedLineNumber;
		
		if ((this.firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER) &&
			(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER))
		{
			preferedLineNumber = lastLineNumber-this.firstLineNumber;
		}
		else
		{
			preferedLineNumber = LayoutBlockConstants.UNLIMITED_LINE_COUNT;
		}
				
		this.layoutBlockList.add(new InstructionLayoutBlock(
			LayoutBlockConstants.INSTRUCTION,
			this.firstLineNumber, lastLineNumber, 
			preferedLineNumber, preferedLineNumber, preferedLineNumber, 
			this.classFile, this.method, this.instruction, 
			this.offset1, in.offset));
		
		this.firstLineNumber = parent.lineNumber;
		this.offset1 = in.offset;
		
		// Add blocks for inner class body
		ClassFileLayouter.CreateBlocksForBodyOfAnonymousClass(
			this.preferences, innerClassFile, this.layoutBlockList);
	}
}
