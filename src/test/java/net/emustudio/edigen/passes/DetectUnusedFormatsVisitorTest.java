package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class DetectUnusedFormatsVisitorTest {
    private Decoder decoder;
    private Disassembler disassembler;
    private Specification specification;

    @Before
    public void setUp() {
        decoder = new Decoder();
        disassembler = new Disassembler();
        specification = new Specification(decoder, disassembler);
    }


    @Test(expected = SemanticException.class)
    public void testUnusedFormatIsDetected() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant(new Subrule("B")));
        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        decoder.addChildren(
                mkRule("A")
                        .setRoot(true, "A")
                        .addChildren(
                                nest(
                                        mkVariant("Bs"),
                                        mkSubrule("B").setRule(rb)),
                                nest(
                                        mkVariant(),
                                        mkSubrule("C").setRule(rc))),
                rb,
                rc
        );

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

        new DetectUnusedFormatsVisitor().visit(specification);
    }

    @Test
    public void testDifferentParamsOrderIsOk() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        decoder.addChildren(
                nest(
                        mkRule("A").setRoot(true, "A"),
                        mkVariant("A").addChildren(
                                mkSubrule("B").setRule(rb),
                                mkSubrule("C").setRule(rc))),
                rb,
                rc
        );

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("C"),
                        new Value("B")
                )
        );

        new DetectUnusedFormatsVisitor().visit(specification);
    }

    @Test(expected = SemanticException.class)
    public void testPartialPathIsNotAllowed() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        decoder.addChildren(
                nest(
                        mkRule("A").setRoot(true, "A"),
                        mkVariant("A").addChildren(
                                mkSubrule("B").setRule(rb),
                                mkSubrule("C").setRule(rc))),
                rb,
                rc
        );

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B")
                )
        );

        new DetectUnusedFormatsVisitor().visit(specification);
    }

    @Test(expected = SemanticException.class)
    public void testMissingParamsAreDetected() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        decoder.addChildren(
                nest(
                        mkRule("A").setRoot(true, "A"),
                        mkVariant("As").addChildren(
                                mkSubrule("B").setRule(rb),
                                mkSubrule("C")
                        )),
                rb
        );

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

        new DetectUnusedFormatsVisitor().visit(specification);
    }

    @Test
    public void testNestedSubrulesAreOk() throws SemanticException {
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

        decoder.addChildren(
                nest(
                        mkRule("A").setRoot(true, "A"),
                        mkVariant().addChildren(
                                mkSubrule("B").setRule(rb),
                                mkSubrule("C").setRule(rc)
                        )),
                rb,
                rc,
                rd
        );

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("B"),
                        new Value("D")
                )
        );

        new DetectUnusedFormatsVisitor().visit(specification);
    }

    @Test
    public void testMultipleRuleNamesAreOk() throws SemanticException {
        Rule rb = nest(
                mkRule("B", "B2"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        decoder.addChildren(
                nest(
                        mkRule("A").setRoot(true, "A"),
                        mkVariant("A").addChildren(
                                mkSubrule("B2").setRule(rb))),
                rb
        );

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B2")
                )
        );

        new DetectUnusedFormatsVisitor().visit(specification);
    }

    @Test(expected = SemanticException.class)
    public void testDifferentVariantsInNotRootRule() throws SemanticException {
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

        decoder.addChildren(
                nest(
                        mkRule("A").setRoot(true, "A"),
                        mkVariant("B"),
                        mkSubrule("B").setRule(rb)),
                rb
        );

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B"),
                        new Value("C"),
                        new Value("D")
                )
        );

        new DetectUnusedFormatsVisitor().visit(specification);
    }
}
