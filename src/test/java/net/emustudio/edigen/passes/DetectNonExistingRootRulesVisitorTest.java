package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class DetectNonExistingRootRulesVisitorTest {

    @Test(expected = SemanticException.class)
    public void testNonExistingRootRulesAreDetected() throws SemanticException {
        Set<String> rootRuleNames = new HashSet<>();
        rootRuleNames.add("rule");

        Decoder decoder = new Decoder(rootRuleNames);
        decoder.accept(new DetectNonExistingRootRulesVisitor());
    }
}
