package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class MergePatternsVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        decoder = new Decoder();
    }

    @Test
    public void testSubruleWithoutLengthDoesNotGenerateMaskAndPattern() throws SemanticException {
        // It is not necessary to match no-length subrule since it can exist only at the end of the variant.
        // Decoder will continue matching the subrule later.

        // rule = "something": nolength;
        // nolength = arg: arg(20);

        Rule r1 = nest(
                mkRule("something"),
                mkVariant("something"),
                mkSubrule("nolength")
        );

        Rule r2 = nest(
                mkRule("nolength"),
                mkVariant(mkSubrule("arg")),
                mkSubrule("arg", 20, null)
        );

        decoder.addChildren(r1, r2);
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new MergePatternsVisitor());

        assertTreesAreEqual(r1, nest(
                mkRule("something"),
                mkVariant("something").addChildren(
                        mkSubrule("nolength", null, null, 0, r2),
                        mkMask(""), // no mask
                        mkPattern("") // no pattern
                )
        ));
    }

    @Test
    public void testSubruleWithoutPrePatternGeneratesFalseMaskAndPattern() throws SemanticException {
        // rule = "something": subrule(8);
        Rule r = nest(
                mkRule("something"),
                mkVariant("something"),
                mkSubrule("subrule", 8, null)
        );

        decoder.addChild(r);
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new MergePatternsVisitor());

        Rule subrule = mkRule("subrule");
        nest(
                subrule,
                mkVariant(mkSubrule("subrule", 8, null, 0, subrule)).addChildren(
                        mkSubrule("subrule", 8, null, 0, subrule),
                        mkMask("00000000"),
                        mkPattern("00000000")
                )
        );
        assertTreesAreEqual(r, nest(
                mkRule("something"),
                mkVariant("something").addChildren(
                        mkSubrule("subrule", 8, null, 0, subrule),
                        mkMask("00000000"),
                        mkPattern("00000000")
                ))
        );
    }

    @Test
    public void testSubruleWithPrePatternGeneratesTrueMaskAndPrePatternBits() throws SemanticException {
        // rule = "something": subrule[1101](8);

        Rule r = nest(
                mkRule("something"),
                mkVariant("something"),
                mkSubrule("subrule", 8, mkPattern("1101"))
        );

        decoder.addChild(r);
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new MergePatternsVisitor());

        Rule subrule = mkRule("subrule");
        nest(
                subrule,
                mkVariant(mkSubrule("subrule", 8, mkPattern("1101"), 0, subrule)).addChildren(
                        mkSubrule("subrule", 8, mkPattern("1101"), 0, subrule),
                        mkMask("11110000"),
                        mkPattern("11010000")
                )
        );
        assertTreesAreEqual(r, nest(
                mkRule("something"),
                mkVariant("something").addChildren(
                        mkSubrule("subrule", 8, mkPattern("1101"), 0, subrule),
                        mkMask("11110000"),
                        mkPattern("11010000")
                ))
        );
    }

    @Test
    public void testConstantGeneratesTrueMaskAndConstantBits() throws SemanticException {
        // rule = "something": 0xAA55;

        Rule r = nest(
                mkRule("something"),
                mkVariant("something"),
                mkPattern("1010101001010101")
        );

        decoder.addChild(r);
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new MergePatternsVisitor());

        assertTreesAreEqual(r, nest(
                mkRule("something"),
                mkVariant("something").addChildren(
                        mkMask("1111111111111111"),
                        mkPattern("1010101001010101")
                )
        ));
    }

    @Test(expected = SemanticException.class)
    public void testPrePatternOfSubruleCannotBeLongerThanSubruleLength() throws SemanticException {
        // rule = "something": subrule[1101](2);

        Rule r = nest(
                mkRule("something"),
                mkVariant("something"),
                mkSubrule("subrule", 2, mkPattern("1101"))
        );

        decoder.addChild(r);
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new MergePatternsVisitor());
    }
}
