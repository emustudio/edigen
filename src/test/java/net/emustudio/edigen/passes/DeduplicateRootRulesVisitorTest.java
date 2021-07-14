package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import org.junit.Test;

import java.util.Set;

import static net.emustudio.edigen.passes.PassUtils.mkRule;
import static org.junit.Assert.assertEquals;

public class DeduplicateRootRulesVisitorTest {

    @Test
    public void testRootRulesAreDeduplicated() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder(
                "rule1", "rule2", "rule5", "rule4", "rule1", "rule4"
        ).addChildren(
                mkRule("rule1"),
                mkRule("rule2", "rule3", "rule4"),
                mkRule("rule5")
        );

        decoder.accept(new DeduplicateRootRulesVisitor());
        assertEquals(Set.of("rule1", "rule2", "rule5"), decoder.getRootRuleNames());
    }
}
