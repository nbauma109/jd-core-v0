package jd.core.model.layout.block;

import java.util.List;

/**
 * The declaration of a compile-time constant: its value is inlined where it is used, so no other declaration depends
 * on where it is written.
 */
public class ConstantFieldLayoutBlock extends SubListLayoutBlock
{
    public ConstantFieldLayoutBlock(
        byte tag, List<LayoutBlock> subList,
        int firstLineNumber, int lastLineNumber, int preferedLineCount)
    {
        super(tag, subList, firstLineNumber, lastLineNumber, preferedLineCount);
    }
}
