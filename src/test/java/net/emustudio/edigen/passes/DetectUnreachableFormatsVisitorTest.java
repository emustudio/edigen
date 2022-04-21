package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static net.emustudio.edigen.passes.PassUtils.*;

public class DetectUnreachableFormatsVisitorTest {
    private Decoder decoder;
    private Disassembler disassembler;
    private Specification specification;
    private Rule rootRule;

    @Before
    public void setUp() {
        decoder = new Decoder("A");
        rootRule = mkRule("A").setRoot(true, "A");
        decoder.addChildren(rootRule);
        Set<Rule> rootR = new HashSet<>();
        rootR.add(rootRule);
        decoder.setRootRules(rootR);

        disassembler = new Disassembler();
        specification = new Specification(decoder, disassembler);
    }

    @Test
    public void testMultipleVariants() throws SemanticException {
        // R A
        //   V (r)
        //     S B
        //     S D
        //   V (r)
        //     S E
        //   V
        //     S C
        //       V (r)
        //         S F
        //         S G
        //   V (r)
        //     S K
        //       V (r)
        //         S F
        //       V (r)
        //         S G
        //     S H
        //       V (r)
        //         S I
        //       V (r)
        //         S J

        Rule b = nest(mkRule("B"), mkVariant(new Subrule("B")));
        Rule d = nest(mkRule("D"), mkVariant(new Subrule("D")));
        Rule e = nest(mkRule("E"), mkVariant(new Subrule("E")));
        Rule f = nest(mkRule("F"), mkVariant(new Subrule("F")));
        Rule g = nest(mkRule("G"), mkVariant(new Subrule("G")));
        Rule i = nest(mkRule("I"), mkVariant(new Subrule("I")));
        Rule j = nest(mkRule("J"), mkVariant(new Subrule("J")));

        Rule c = nest(
                mkRule("C"),
                mkVariant("FG").addChildren(
                        mkSubrule("F").setRule(f),
                        mkSubrule("G").setRule(g)));

        Rule k = (Rule) mkRule("K").addChildren(
                nest(mkVariant("F"), mkSubrule("F").setRule(f)),
                nest(mkVariant("G"), mkSubrule("G").setRule(g)));

        Rule h = (Rule) mkRule("H").addChildren(
                nest(mkVariant("I"), mkSubrule("I").setRule(i)),
                nest(mkVariant("J"), mkSubrule("J").setRule(j)));


        rootRule.addChildren(
                mkVariant("BD").addChildren(
                        mkSubrule("B").setRule(b),
                        mkSubrule("D").setRule(d)),
                nest(mkVariant("E"), mkSubrule("E").setRule(e)),
                nest(mkVariant(), mkSubrule("C").setRule(c)),
                mkVariant("K").addChildren(
                        mkSubrule("K").setRule(k),
                        mkSubrule("H").setRule(h)));
        decoder.addChildren(
                b, c, d, e, f, g, h, i, j, k
        );

        runTest();
    }


    @Test(expected = SemanticException.class)
    public void testUnreachableFormatIsDetected() throws SemanticException {
        // Rule A
        //   Variant (return "Bs")
        //     Subrule B
        //   Variant
        //     Subrule C
        //
        // Rule B
        //   Variant (return subrule B)
        //
        // Rule C
        //   Variant (return "Cs")

        // Possibilities:
        //  A,B
        //  A,C

        Rule rb = nest(
                mkRule("B"),
                mkVariant(new Subrule("B")));
        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        rootRule.addChildren(
                nest(
                        mkVariant("Bs"),
                        mkSubrule("B").setRule(rb)),
                nest(
                        mkVariant(),
                        mkSubrule("C").setRule(rc)));
        decoder.addChildren(rb, rc);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B")
                ),
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B"),
                        new Value("C") // impossible both "B" and "C"
                )
        );

        runTest();
    }

    @Test
    public void testDifferentParamsOrderIsOk() throws SemanticException {
        // Rule A
        //   Variant (return "A")
        //     Subrule B
        //     Subrule C
        //
        // Rule B
        //   Variant (return "Bs")
        //     Subrule B
        //
        // Rule C
        //   Variant (return "Cs")

        // Possibilities:
        // A,B,C

        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        rootRule.addChildren(
                mkVariant("A").addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C").setRule(rc)));

        decoder.addChildren(rb, rc);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("C"),
                        new Value("B")
                )
        );

        runTest();
    }

    @Test(expected = SemanticException.class)
    public void testPartialPathIsNotAllowed() throws SemanticException {
        // Rule A
        //   Variant (return "A")
        //     Subrule B
        //     Subrule C
        //
        // Rule B
        //   Variant (return "Bs")
        //     Subrule B
        //
        // Rule C
        //   Variant (return "Cs")
        //
        // Possibilities:
        //   A,B,C
        //
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        rootRule.addChildren(
                mkVariant("A").addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C").setRule(rc)));
        decoder.addChildren(rb, rc);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B")
                )
        );

        runTest();
    }

    @Test(expected = SemanticException.class)
    public void testMissingParamsAreDetected() throws SemanticException {
        // Rule A
        //   Variant (return "As")
        //     Subrule B
        //     Subrule C
        //
        // Rule B
        //   Variant (return "Bs")
        //     Subrule B
        //
        // Possibilities:
        //   A,B
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        rootRule.addChildren(
                mkVariant("As").addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C")
                ));
        decoder.addChildren(rb);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B")
                ),
                new Format("").addChildren(
                        new Value("A"),
                        new Value("C"),
                        new Value("B")
                )
        );

        runTest();
    }

    @Test
    public void testNestedSubrulesAreOk() throws SemanticException {
        // Rule A
        //   Variant
        //     Subrule B
        //     Subrule C
        //
        // Rule B
        //   Variant (return subrule B)
        //
        // Rule D
        //   Variant (return "Ds")
        //
        // Rule C
        //   Variant
        //     Subrule D
        //
        // Possibilities:
        //   B, D
        Rule rb = nest(
                mkRule("B"),
                mkVariant(new Subrule("B")));

        Rule rd = nest(
                mkRule("D"),
                mkVariant("Ds"));

        Rule rc = nest(
                mkRule("C"),
                mkVariant().addChildren(
                        mkSubrule("D").setRule(rd)
                ));

        rootRule.addChildren(
                mkVariant().addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C").setRule(rc)
                ));

        decoder.addChildren(rb, rc, rd);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("B"),
                        new Value("D")
                )
        );

        runTest();
    }

    @Test
    public void testMultipleNestedSubrulesAreOk() throws SemanticException {
        // Rule A
        //   Variant
        //     Subrule B
        //     Subrule F
        //
        // Rule B
        //   Variant
        //     Subrule E
        //   Variant (return "aa")
        //     Subrule C
        //     Subrule D
        //
        // Rule C
        //   Variant (return "Cs")
        //
        // Rule D
        //   Variant (return "Ds")
        //
        // Rule E
        //   Variant (return "Es")
        //
        // Rule F
        //   Variant (return "Fs")
        //
        // Possibilities:
        //   E, F
        //   B, C, D, F
        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        Rule rd = nest(
                mkRule("D"),
                mkVariant("Ds"));

        Rule re = nest(
                mkRule("E"),
                mkVariant("Es"));

        Rule rf = nest(
                mkRule("F"),
                mkVariant("Fs"));


        Rule rb = (Rule) mkRule("C").addChildren(
                mkVariant().addChildren(
                        mkSubrule("E").setRule(re)),
                mkVariant("aa").addChildren(
                        mkSubrule("C").setRule(rc),
                        mkSubrule("D").setRule(rd)));

        rootRule.addChildren(
                mkVariant().addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("F").setRule(rf)
                ));

        decoder.addChildren(rb, rc, rd, re, rf);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("B"),
                        new Value("C"),
                        new Value("D"),
                        new Value("F")
                ),
                new Format("").addChildren(
                        new Value("E"),
                        new Value("F")
                )
        );

        runTest();
    }


    @Test
    public void testMultipleRuleNamesAreOk() throws SemanticException {
        // Rule A
        //   Variant (return "A")
        //     Subrule B2
        //
        // Rule B, B2
        //   Variant (return "Bs")
        //     Subrule B
        //
        // Possibilities:
        //   A, B2

        Rule rb = nest(
                mkRule("B", "B2"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        rootRule.addChildren(
                mkVariant("A").addChildren(
                        mkSubrule("B2").setRule(rb)));
        decoder.addChildren(rb);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B2")
                )
        );

        runTest();
    }

    @Test(expected = SemanticException.class)
    public void testDifferentVariantsInNotRootRule() throws SemanticException {
        // Rule A
        //   Variant (return "B")
        //     Subrule B
        //
        // Rule B
        //   Variant (return "C")
        //     Subrule C
        //   Variant (return "D")
        //     Subrule D
        //
        // Rule C
        //   Variant (return "nested1")
        //
        // Rule D
        //   Variant (return "nested2")
        //
        // Possibilities:
        //   A, B, C
        //   A, B, D
        Rule rc = nest(
                mkRule("nested1"),
                mkVariant("nested1"));
        Rule rd = nest(
                mkRule("nested2"),
                mkVariant("nested2"));

        Rule rb = (Rule) mkRule("B").addChildren(
                nest(
                        mkVariant("C"),
                        mkSubrule("C").setRule(rc)),
                nest(
                        mkVariant("D"),
                        mkSubrule("D").setRule(rd)));

        rootRule.addChildren(
                nest(
                        mkVariant("B"),
                        mkSubrule("B").setRule(rb)));
        decoder.addChildren(rb);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B"),
                        new Value("C"),
                        new Value("D")
                )
        );

        runTest();
    }

    private void runTest() throws SemanticException {
        DetectUnreachableFormatsVisitor visitor = new DetectUnreachableFormatsVisitor();
        visitor.visit(specification);
    }
}
