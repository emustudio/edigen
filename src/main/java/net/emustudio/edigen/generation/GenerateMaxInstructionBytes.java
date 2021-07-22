package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Mask;

import java.io.Writer;

/**
 * Finds out max instruction size in bytes.
 */
public class GenerateMaxInstructionBytes extends Visitor  {
    private final PrettyPrinter printer;
    private int maxBitSize;

    /**
     * Constructs the visitor.
     * @param output the output stream to write the code to
     */
    public GenerateMaxInstructionBytes(Writer output) {
        this.printer = new PrettyPrinter(output);
    }

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);
        printer.write(Integer.toString(maxBitSize / 8 + (((maxBitSize % 8) == 0) ? 0 : 1)));
    }

    /**
     * Detects max bits size of a mask and it's children
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        int maskStart = mask.getStart();
        int maskLength = mask.getBits().getLength();

        maxBitSize = Math.max(maxBitSize, maskStart + maskLength);
        mask.acceptChildren(this);
    }
}
