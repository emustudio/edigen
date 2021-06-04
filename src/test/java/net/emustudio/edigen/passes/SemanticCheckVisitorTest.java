package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

public class SemanticCheckVisitorTest {
    private Decoder decoder;
    private Variant variant;
    private Disassembler disassembler;
    private Format format;

    @Before
    public void setUp() {
        decoder = new Decoder();
        Rule rule = new Rule("rule");
        decoder.addChild(rule);
        variant = new Variant();
        rule.addChild(variant);

        disassembler = new Disassembler();
        format = new Format("");
        disassembler.addChild(format);
    }

    @Test(expected = SemanticException.class)
    public void testNotEndingSubruleMustHaveLengthSpecified() throws SemanticException {
        variant.addChild(new Subrule("subrule1"));
        variant.addChild(new Subrule("subrule2"));
        decoder.accept(new SemanticCheckVisitor());
    }

    @Test
    public void testEndingSubruleDoesntNeedToHaveLengthSpecified() throws SemanticException {
        variant.addChild(new Subrule("subrule1"));
        decoder.accept(new SemanticCheckVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testNonReturningValueUsedInDisassemblerThrows() throws SemanticException {
        Value value = new Value("rule");
        format.addChild(value);
        Specification specification = new Specification(decoder, disassembler);
        specification.accept(new ResolveNamesVisitor());
        specification.accept(new SemanticCheckVisitor());
    }

    @Test
    public void testReturningValueCanBeUsedInDisassembler() throws SemanticException {
        Value value = new Value("rule");
        format.addChild(value);
        variant.setReturnString("at least something");
        Specification specification = new Specification(decoder, disassembler);
        specification.accept(new ResolveNamesVisitor());
        specification.accept(new SemanticCheckVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testTwoValuesUsedInFormatCannotBeTheSame() throws SemanticException {
        Value value = new Value("rule");
        format.addChild(value);

        Format format2 = new Format("");
        format2.addChild(value);
        disassembler.addChild(format2);

        variant.setReturnString("at least something");
        Specification specification = new Specification(decoder, disassembler);
        specification.accept(new ResolveNamesVisitor());
        specification.accept(new SemanticCheckVisitor());
    }
}
