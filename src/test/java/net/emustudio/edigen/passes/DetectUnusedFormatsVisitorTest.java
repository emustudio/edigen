package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class DetectUnusedFormatsVisitorTest {

    @Test(expected = SemanticException.class)
    public void testUnusedFormatIsDetected() throws SemanticException {
        Decoder decoder = new Decoder();
        Disassembler disassembler = new Disassembler();
        Specification specification = new Specification(decoder, disassembler);

        decoder.addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used", 7, null)
                ), nest(
                        mkRule("another"),
                        mkVariant("v"),
                        mkSubrule("implicit")
                )
        );

        decoder.addChildren(
                new Format("").addChildren(
                        new Value("rule"),
                        new Value("used")
                ),
                new Format("").addChildren(
                        new Value("rule"),
                        new Value("implicit") // impossible
                )
        );

        new DetectUnusedFormatsVisitor().visit(specification);
    }
}
