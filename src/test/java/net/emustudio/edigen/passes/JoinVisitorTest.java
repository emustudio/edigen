package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static net.emustudio.edigen.passes.PassUtils.findMask;
import static net.emustudio.edigen.passes.PassUtils.findPattern;
import static org.junit.Assert.assertEquals;

public class JoinVisitorTest {
    private Decoder decoder;
    private Variant variant;

    @Before
    public void setUp() {
        decoder = new Decoder();
        Rule rule = new Rule("rule");
        decoder.addChild(rule);
        variant = new Variant();
        rule.addChild(variant);
    }

    @Test
    public void testSubruleWithoutLengthDoesNotGenerateMaskAndPattern() throws SemanticException {
        // It is not necessary to match no-length subrule since it can exist only at the end of the variant.
        // Decoder will continue matching the subrule later.

        // rule = "something": nolength;
        // nolength = arg: arg(20);

        variant.setReturnString("something");
        Subrule nolength = new Subrule("nolength", null, null);
        variant.addChild(nolength);

        Rule nolengthDefinition = new Rule("nolength");
        Variant nolengthVariant = new Variant();
        nolengthVariant.setReturnSubrule(new Subrule("arg"));
        nolengthVariant.addChild(new Subrule("arg", 20, null));
        nolengthDefinition.addChild(nolengthVariant);

        decoder.addChild(nolengthDefinition);

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new JoinVisitor());

        assertEquals(0, findMask(variant).getBits().getLength());
        assertEquals(0, findPattern(variant).getBits().getLength());
    }

    @Test
    public void testSubruleWithoutPrePatternGeneratesFalseMaskAndPattern() throws SemanticException {
        // rule = "something": subrule(8);

        variant.setReturnString("something");
        Subrule subrule = new Subrule("subrule", 8, null);
        variant.addChild(subrule);

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new JoinVisitor());

        assertEquals("00000000", findMask(variant).getBits().toString());
        assertEquals("00000000", findPattern(variant).getBits().toString());
    }

    @Test
    public void testSubruleWithPrePatternGeneratesTrueMaskAndPrePatternBits() throws SemanticException {
        // rule = "something": subrule[1101](8);

        variant.setReturnString("something");
        Subrule subrule = new Subrule("subrule", 8, new Pattern(BitSequence.fromBinary("1101")));
        variant.addChild(subrule);

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new JoinVisitor());

        assertEquals("11110000", findMask(variant).getBits().toString());
        assertEquals("11010000", findPattern(variant).getBits().toString());
    }

    @Test
    public void testConstantGeneratesTrueMaskAndConstantBits() throws SemanticException {
        // rule = "something": 0xAA55;

        variant.setReturnString("something");
        variant.addChild(new Pattern(BitSequence.fromHexadecimal("AA55")));

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new JoinVisitor());

        assertEquals("1111111111111111", findMask(variant).getBits().toString());
        assertEquals("1010101001010101", findPattern(variant).getBits().toString());
    }

    @Test(expected = SemanticException.class)
    public void testPrePatternOfSubruleCannotBeLongerThanSubruleLength() throws SemanticException {
        // rule = "something": subrule[1101](2);

        variant.setReturnString("something");
        Subrule subrule = new Subrule("subrule", 2, new Pattern(BitSequence.fromBinary("1101")));
        variant.addChild(subrule);

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new JoinVisitor());
    }
}
