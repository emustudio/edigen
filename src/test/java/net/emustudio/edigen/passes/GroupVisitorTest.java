package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class GroupVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }

    @Test
    public void testMasksAndPatternsAreGrouped() throws SemanticException {
        Rule rule = (Rule) mkRule("rule").addChildren(
                nest(
                        mkMask("111"),
                        mkPattern("110"),
                        mkVariant()
                ), nest(
                        mkMask("111"),
                        mkPattern("111"),
                        mkVariant()
                )
        );

        decoder.addChild(rule);
        decoder.accept(new GroupVisitor());

        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkMask("111").addChildren(
                        nest(
                                mkPattern("110"),
                                mkVariant()
                        ), nest(
                                mkPattern("111"),
                                mkVariant()
                        )
                )
        ));
    }

    @Test
    public void testTwoRulesWithSameMasksAreKeptSeparate() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder().addChildren(
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                ),
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                )
        );

        decoder.accept(new GroupVisitor());

        assertTreesAreEqual(decoder, new Decoder().addChildren(
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                ),
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                )
        ));
    }
}
