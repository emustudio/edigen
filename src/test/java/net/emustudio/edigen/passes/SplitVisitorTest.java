package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;
import static net.emustudio.edigen.passes.SplitVisitor.BITS_PER_PIECE;

public class SplitVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        decoder = new Decoder();
    }

    @Test
    public void testVerticalSplit() throws SemanticException {
        String maskPatternString = mkString(BITS_PER_PIECE * 3, '0');
        String expectedMaskPatternString = mkString(BITS_PER_PIECE, '0');

        Rule rule = nest(
                mkRule("rule"),
                mkVariant().addChildren(
                        mkMask(maskPatternString),
                        mkPattern(maskPatternString)
                )
        );

        decoder.addChild(rule);
        decoder.accept(new SplitVisitor());

        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkVariant(),
                mkMask(expectedMaskPatternString, 0),
                mkPattern(expectedMaskPatternString),
                mkMask(expectedMaskPatternString, BITS_PER_PIECE),
                mkPattern(expectedMaskPatternString),
                mkMask(expectedMaskPatternString, 2 * BITS_PER_PIECE),
                mkPattern(expectedMaskPatternString)
        ));
    }

    @Test
    public void testMaskShorterThanBitsPerPieceIsNotSplit() throws SemanticException {
        String maskPatternString = mkString(BITS_PER_PIECE, '0');

        Rule rule = nest(
                mkRule("rule"),
                mkVariant().addChildren(
                        mkMask(maskPatternString),
                        mkPattern(maskPatternString)
                ));

        decoder.addChild(rule);
        decoder.accept(new SplitVisitor());

        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkVariant(),
                mkMask(maskPatternString, 0),
                mkPattern(maskPatternString)
        ));
    }

    @Test
    public void testMaskIsSplitProperly() throws SemanticException {
        String maskPatternString = mkString(BITS_PER_PIECE + 1, '0');

        Rule rule = nest(
                mkRule("rule"),
                mkVariant().addChildren(
                        mkMask(maskPatternString),
                        mkPattern(maskPatternString)
                )
        );

        decoder.addChild(rule);
        decoder.accept(new SplitVisitor());

        String bitsPerPieceMaskPatternString = mkString(BITS_PER_PIECE, '0');
        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkVariant(),
                mkMask(bitsPerPieceMaskPatternString, 0),
                mkPattern(bitsPerPieceMaskPatternString),
                mkMask("0", BITS_PER_PIECE),
                mkPattern("0")
        ));
    }
}
