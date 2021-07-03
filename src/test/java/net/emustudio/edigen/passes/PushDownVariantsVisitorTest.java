package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class PushDownVariantsVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        decoder = new Decoder();
    }

    @Test
    public void testVariantsAreMovedToBottom() throws SemanticException {
        Rule rule = (Rule) nest(
                mkRule("rule").addChild(nest(
                        mkVariant(),
                        mkMask("111"),
                        mkPattern("001")
                )),
                mkVariant(),
                mkMask("111"),
                mkPattern("001"),
                mkMask("111"),
                mkPattern("001")
        );

        decoder.addChild(rule);
        decoder.accept(new PushDownVariantsVisitor());

        assertTreesAreIsomorphic(rule, nest(
                mkRule("rule").addChild(nest(
                        mkMask("111"),
                        mkPattern("001"),
                        mkVariant()
                )),
                mkMask("111"),
                mkPattern("001"),
                mkMask("111"),
                mkPattern("001"),
                mkVariant()
        ));
    }

    @Test
    public void testVariantWithoutMaskIsKept() throws SemanticException {
        Rule rule = nest(
                mkRule("rule"),
                mkVariant()
        );
        decoder.addChild(rule);
        decoder.accept(new PushDownVariantsVisitor());

        assertTreesAreIsomorphic(rule, nest(
                mkRule("rule"),
                mkVariant()
        ));
    }
}
