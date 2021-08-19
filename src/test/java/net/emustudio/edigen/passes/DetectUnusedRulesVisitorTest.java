package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class DetectUnusedRulesVisitorTest {

    @Test(expected = SemanticException.class)
    public void testUnusedRuleIsDetected() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder().addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used", 7, null)
                ), nest(
                        mkRule("unused"),
                        mkVariant("haha"),
                        mkSubrule("unused", 5, null)
                )
        );

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new DetectUnusedRulesVisitor());
    }

    @Test
    public void testRuleUsedLaterIsNotDetectedAsUnused() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder().addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used2", 7, null)
                ), nest(
                        mkRule("used2"),
                        mkVariant("haha"),
                        mkSubrule("unused", 5, null)
                )
        );
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new DetectUnusedRulesVisitor());
    }

    @Test
    public void testRootRulesAreNotTreatedAsUnused() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder("rule", "unused").addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used", 7, null)
                ), nest(
                        mkRule("unused"),
                        mkVariant("haha"),
                        mkSubrule("unused", 5, null)
                )
        );

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new DetectUnusedRulesVisitor());
    }
}
