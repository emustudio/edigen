package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.createVariantWithMaskAndPattern;
import static net.emustudio.edigen.passes.PassUtils.mkString;
import static net.emustudio.edigen.passes.SplitVisitor.BITS_PER_PIECE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SplitVisitorTest {
    private Decoder decoder;
    private Rule rule;

    @Before
    public void setUp() {
        decoder = new Decoder();
        rule = new Rule("rule");
        decoder.addChild(rule);
    }

    @Test
    public void testVerticalSplit() throws SemanticException {
        String maskPatternString = mkString(BITS_PER_PIECE*3, '0');
        String expectedMaskPatternString = mkString(BITS_PER_PIECE, '0');

        Variant variant = createVariantWithMaskAndPattern(maskPatternString, maskPatternString);
        rule.addChild(variant);
        decoder.accept(new SplitVisitor());

        // level 1
        Mask child = assertMaskChild(variant, expectedMaskPatternString);
        Pattern child1 = assertPatternChild(child, expectedMaskPatternString);

        // level 2
        Mask child2 = assertMaskChild(child1, expectedMaskPatternString);
        Pattern child3 = assertPatternChild(child2, expectedMaskPatternString);

        // level 3
        Mask child4 = assertMaskChild(child3, expectedMaskPatternString);
        Pattern child5 = assertPatternChild(child4, expectedMaskPatternString);

        assertEquals(0, child5.childCount());
    }

    @Test
    public void testMaskShorterThanBitsPerPieceIsNotSplit() throws SemanticException {
        String maskPatternString = mkString(BITS_PER_PIECE, '0');

        Variant variant = createVariantWithMaskAndPattern(maskPatternString, maskPatternString);
        rule.addChild(variant);
        decoder.accept(new SplitVisitor());

        // level 1
        Mask child = assertMaskChild(variant, maskPatternString);
        Pattern child1 = assertPatternChild(child, maskPatternString);

        assertEquals(0, child1.childCount());
    }

    @Test
    public void testMaskIsSplitProperly() throws SemanticException {
        String maskPatternString = mkString(BITS_PER_PIECE + 1, '0');

        Variant variant = createVariantWithMaskAndPattern(maskPatternString, maskPatternString);
        rule.addChild(variant);
        decoder.accept(new SplitVisitor());

        // level 1
        String expectedMaskPatternString = mkString(BITS_PER_PIECE, '0');
        Mask child = assertMaskChild(variant, expectedMaskPatternString);
        Pattern child1 = assertPatternChild(child, expectedMaskPatternString);

        // level 2
        expectedMaskPatternString = mkString(1, '0');
        Mask child2 = assertMaskChild(child1, expectedMaskPatternString);
        Pattern child3 = assertPatternChild(child2, expectedMaskPatternString);

        assertEquals(0, child3.childCount());
    }

    private Mask assertMaskChild(TreeNode node, String expected) {
        assertEquals(1, node.childCount());
        TreeNode child = node.getChild(0);
        assertTrue(child instanceof Mask);
        assertEquals(expected, ((Mask)child).getBits().toString());
        return (Mask)child;
    }

    private Pattern assertPatternChild(TreeNode node, String expected) {
        assertEquals(1, node.childCount());
        TreeNode child = node.getChild(0);
        assertTrue(child instanceof Pattern);
        assertEquals(expected, ((Pattern)child).getBits().toString());
        return (Pattern)child;
    }
}
