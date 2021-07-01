package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class SortVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }

    @Test
    public void testMasksAreSortedFromShortestToLongest() throws SemanticException {
        // stable sort
        Rule rule = (Rule) mkRule("rule")
                .addChildren(
                        nest(
                                mkVariant(),
                                mkMask("000011")
                        ), nest(
                                mkVariant(),
                                mkMask("00000")
                        ), nest(
                                mkVariant(),
                                mkMask("1111")
                        ), nest(
                                mkVariant(),
                                mkMask("00001")
                        ), nest(
                                mkVariant(),
                                mkMask("0000")
                        )
                );

        decoder.addChild(rule);
        decoder.accept(new SortVisitor());

        assertTreesAreEqual(rule, mkRule("rule")
                .addChildren(
                        nest(
                                mkVariant(),
                                mkMask("1111")
                        ), nest(
                                mkVariant(),
                                mkMask("0000")
                        ), nest(
                                mkVariant(),
                                mkMask("00000")
                        ), nest(
                                mkVariant(),
                                mkMask("00001")
                        ), nest(
                                mkVariant(),
                                mkMask("000011")
                        )
                ));
    }

    @Test
    public void testNoMasksNoThrow() throws SemanticException {
        Rule rule = new Rule("rule");
        Decoder decoder = new Decoder();
        decoder.addChild(rule);
        decoder.accept(new SortVisitor());
    }
}
