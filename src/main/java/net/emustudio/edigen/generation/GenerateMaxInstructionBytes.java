package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.Subrule;

import java.io.Writer;

/**
 * Finds out max instruction size in bytes.
 */
public class GenerateMaxInstructionBytes extends Visitor  {
    private final PrettyPrinter printer;
    private int maxBitSize;
    private int lastStart;
    private int ruleLevel = 0;

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
        int maxBytes = (int)Math.max(1, Math.ceil(maxBitSize / 8.0));
        printer.write(Integer.toString(maxBytes));
    }

    @Override
    public void visit(Rule rule) throws SemanticException {
        ruleLevel++;
        if (ruleLevel == 1) {
            lastStart = 0;
        }
        rule.acceptChildren(this);
    }

    /**
     * Detects max bits size of a mask and its children
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        int maskLength = mask.getBits().getLength();

        maxBitSize = Math.max(maxBitSize, lastStart + maskLength);
        mask.acceptChildren(this);
    }

    @Override
    public void visit(Subrule subrule) throws SemanticException {
        lastStart += subrule.getStart();
        subrule.getRule().accept(this);
        lastStart -= subrule.getStart();
    }
}
