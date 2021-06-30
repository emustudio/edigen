package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoveVariantsVisitorTest {

    // Expectation of a tree at input, e.g.:
    // Rule: rule
    //  Variant
    //    Mask: 111
    //      Pattern: 001
    //        Mask: 111
    //          Pattern: 001
    //  Variant
    //    Mask: 111
    //      Pattern: 001
    //
    // Expectation of the tree at output:
    //  Rule: rule
    //  Mask: 111
    //    Pattern: 001
    //      Mask: 111
    //        Pattern: 001
    //          Variant
    //  Mask: 111
    //    Pattern: 001
    //      Variant

    @Test
    public void testVariantsAreMovedToBottom() throws SemanticException {
        Rule rule = (Rule)chain(
                new Rule("rule"),
                new Variant(),
                mask("111"),
                pattern("001"),
                mask("111"),
                pattern("001")
        );
        rule.addChild(chain(
                new Variant(),
                mask("111"),
                pattern("001")
        ));

        Decoder decoder = new Decoder();
        decoder.addChild(rule);

        decoder.accept(new PrintVisitor("before"));
        decoder.accept(new MoveVariantsVisitor());
        decoder.accept(new PrintVisitor("after"));

        // testing results
        assertEquals(2, rule.childCount());
        TreeNode rmask1 = rule.getChild(0);
        TreeNode rmask2 = rule.getChild(1);

        assertEquals(1, rmask1.childCount());
        assertEquals(1, rmask2.childCount());
        TreeNode rpat1 = rmask1.getChild(0);
        TreeNode rpat2 = rmask2.getChild(0);

        assertEquals(1, rpat1.childCount());
        assertEquals(1, rpat2.childCount());

        TreeNode rmask3 = rpat1.getChild(0);
        TreeNode variant = rpat2.getChild(0);

        assertEquals(1, rmask3.childCount());
        assertEquals(0, variant.childCount());

        TreeNode rpat3 = rmask3.getChild(0);
        assertEquals(1, rpat3.childCount());

        assertTrue(variant instanceof Variant);

        TreeNode variant2 = rpat3.getChild(0);
        assertEquals(0, variant2.childCount());
        assertTrue(variant2 instanceof Variant);
    }

    @Test
    public void testVariantWithoutMaskIsKept() throws SemanticException {
        Rule rule = (Rule)chain(
                new Rule("rule"),
                new Variant()
        );
        Decoder decoder = new Decoder();
        decoder.addChild(rule);
        decoder.accept(new MoveVariantsVisitor());

        assertEquals(1, rule.childCount());
        TreeNode variant = rule.getChild(0);
        assertEquals(0, variant.childCount());
        assertTrue(variant instanceof Variant);
    }
}
