package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class DetectAmbiguousVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }

    @Test(expected = SemanticException.class)
    public void testVariantAmbiguityIsDetected() throws SemanticException {
        // rule = "x": 101 | "y": 101;
        Rule rule = (Rule)mkRule("rule").addChild(
                mkMask("111").addChild(
                        mkPattern("101").addChildren(
                                mkVariant("x"),
                                mkVariant("y")
                        )
                )
        );

        decoder.addChild(rule);
        decoder.accept(new DetectAmbiguousVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testPathAmbiguityIsDetectedForRule() throws SemanticException {
        // rule = "x": 101 | "y": yrule[101](3);
        Rule rule = (Rule)mkRule("rule").addChildren(
                mkMask("111").addChild(
                        mkPattern("101").addChild(
                                mkVariant("x")
                        )
                ),
                mkMask("101").addChild(
                        mkPattern("101").addChild(
                                mkVariant("101")
                        )
                )
        );

        decoder.addChild(rule);
        decoder.accept(new DetectAmbiguousVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testPathAmbiguityIsDetectedForPatternBelowRule() throws SemanticException {
        // rule = subrule(6);
        // subrule = 101 subrule2;
        // subrule2 = "x": 101 | "y": yrule[101](3);
        Rule rule = (Rule)mkRule("rule").addChild(
                mkMask("111111").addChild(
                        mkPattern("101").addChildren(
                                mkMask("111").addChild(
                                        mkPattern("101").addChild(
                                                mkVariant("x")
                                        )
                                ),
                                mkMask("101").addChild(
                                        mkPattern("101").addChild(
                                                mkVariant("y")
                                        )
                                )
                        )
                )
        );

        decoder.addChild(rule);
        decoder.accept(new DetectAmbiguousVisitor());
    }
}
