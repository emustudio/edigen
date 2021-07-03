package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class NarrowMasksVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }


    @Test
    public void testMasksAreNarrowed() throws SemanticException {
        Rule rule = (Rule) mkRule("rule").addChildren(
                mkMask("110").addChild(mkPattern("110")),
                mkMask("010").addChild(mkPattern("010")),
                mkMask("1111").addChild(mkPattern("1110")),
                mkMask("0010").addChild(mkPattern("0010"))
        );

        decoder.addChild(rule);
        decoder.accept(new NarrowMasksVisitor());

        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkMask("110").addChildren(
                        mkPattern("110"), nest(
                                mkPattern(""),
                                mkMask("010").addChildren(
                                        mkPattern("010"),
                                        nest(
                                                mkPattern(""),
                                                mkMask("1111").addChildren(
                                                        mkPattern("1110"),
                                                        nest(
                                                                mkPattern(""),
                                                                mkMask("0010"),
                                                                mkPattern("0010")
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));
    }
}
