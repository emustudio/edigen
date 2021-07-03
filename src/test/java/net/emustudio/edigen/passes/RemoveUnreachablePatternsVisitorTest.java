package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.Decoder;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class RemoveUnreachablePatternsVisitorTest {

    @Test
    public void testUnreachablePatternsAreRemoved() throws SemanticException {
        Decoder decoder = nest(
                new Decoder(),
                mkRule("rule"),
                mkMask("000"),
                mkPattern("111"),
                mkVariant("x")
        );

        decoder.accept(new RemoveUnreachablePatternsVisitor());

        assertTreesAreEqual(decoder, nest(
                new Decoder(),
                mkRule("rule"),
                mkMask("000"),
                mkVariant("x")
        ));
    }
}
