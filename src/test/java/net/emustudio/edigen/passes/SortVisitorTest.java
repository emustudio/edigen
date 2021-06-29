package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.Variant;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.findMaskStrings;
import static org.junit.Assert.assertArrayEquals;

public class SortVisitorTest {

    @Test
    public void testMasksAreSortedFromLongestToShortest() throws SemanticException {
        // stable sort
        Rule rule = new Rule("rule");
        rule.addChild(createVariantWithMask("000011"));
        rule.addChild(createVariantWithMask("00000"));
        rule.addChild(createVariantWithMask("1111"));
        rule.addChild(createVariantWithMask("00001"));
        rule.addChild(createVariantWithMask("0000"));

        Decoder decoder = new Decoder();
        decoder.addChild(rule);
        decoder.accept(new SortVisitor());

        String[] orderedMasks = findMaskStrings(rule).toArray(new String[0]);
        assertArrayEquals(
            new String[] {
                    "1111","0000","00000","00001","000011"
            }, orderedMasks
        );
    }

    @Test
    public void testNoMasksNoThrow() throws SemanticException {
        Rule rule = new Rule("rule");
        Decoder decoder = new Decoder();
        decoder.addChild(rule);
        decoder.accept(new SortVisitor());
    }


    private Variant createVariantWithMask(String bits) {
        Variant variant = new Variant();
        Mask mask = new Mask(BitSequence.fromBinary(bits));
        variant.addChild(mask);
        return variant;
    }
}
