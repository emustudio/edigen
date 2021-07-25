package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static net.emustudio.edigen.passes.PassUtils.mkRule;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DetectRootRulesVisitorTest {

    @Test(expected = SemanticException.class)
    public void testNonExistingRootRulesAreDetected() throws SemanticException {
        Set<String> rootRuleNames = new HashSet<>();
        rootRuleNames.add("rule");

        Decoder decoder = new Decoder(rootRuleNames);
        decoder.accept(new DetectRootRulesVisitor());
    }

    @Test
    public void testRootRulesAreDetected() throws SemanticException {
        Set<String> rootRuleNames = new HashSet<>();
        rootRuleNames.add("a");
        rootRuleNames.add("b");

        Rule a = mkRule("a");
        Rule b = mkRule("b");
        Rule c = mkRule("c");

        Decoder decoder = new Decoder(rootRuleNames);
        decoder.addChildren(a, b, c);

        decoder.accept(new DetectRootRulesVisitor());
        assertTrue(a.isRoot());
        assertTrue(b.isRoot());
        assertFalse(c.isRoot());
    }
}
