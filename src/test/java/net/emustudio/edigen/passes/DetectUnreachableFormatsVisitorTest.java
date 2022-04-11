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
