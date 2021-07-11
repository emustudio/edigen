package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.Decoder;

import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;

/**
 * Visitor which generates calls of root rules in defined order.
 */
public class GenerateRootRuleCallsVisitor extends Visitor {
    private final PrettyPrinter printer;

    public GenerateRootRuleCallsVisitor(Writer output, int baseIndent) {
        this.printer = new PrettyPrinter(Objects.requireNonNull(output), baseIndent);
    }

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        Iterator<String> iterator = decoder.getRootRuleNames().iterator();
        int brackets = 0;
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (iterator.hasNext()) {
                printer.writeLine("try {");
            }
            printer.writeLine(name + "(0);");
            if (iterator.hasNext()) {
                printer.writeLine("} catch (InvalidInstructionException ignored) {");
                brackets++;
            }
        }
        for (int i = 0; i < brackets; i++) {
            printer.writeLine("}");
        }
    }
}
