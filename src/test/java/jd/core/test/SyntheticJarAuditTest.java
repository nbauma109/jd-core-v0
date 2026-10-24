package jd.core.test;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SyntheticJarAuditTest {
    @Test
    public void detectsIdentifiersWithoutReportingCommentsOrLiterals() throws Exception {
        assertEquals(Set.of("access$100", "this$0", "val$value", "$SwitchMap$Kind"),
                SyntheticJarAudit.syntheticIdentifiers("""
                        class Example {
                            // access$000 this$1
                            String text = "lambda$run$0 $assertionsDisabled";
                            int value = access$100(this$0) + val$value + $SwitchMap$Kind[0];
                        }
                        """));
    }
}
