/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.Disassembler;
import net.emustudio.edigen.nodes.Format;
import net.emustudio.edigen.nodes.TreeNode;

import java.io.Writer;
import java.util.Iterator;

/**
 * A visitor which generates the code of the array of disassembler formats.
 */
public class GenerateFormatsVisitor extends Visitor {

    private final PrettyPrinter printer;
    private String formatString;

    /**
     * Constructs the visitor.
     *
     * @param writer the output stream to write the code to
     */
    public GenerateFormatsVisitor(Writer writer) {
        this.printer = new PrettyPrinter(writer);
    }

    /**
     * Writes the formats separated by commas.
     *
     * @param disassembler the disassembler node
     * @throws SemanticException never
     */
    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        Iterator<TreeNode> formats = disassembler.getChildren().iterator();

        while (formats.hasNext()) {
            formats.next().accept(this);

            String separator = formats.hasNext() ? ", " : "";
            printer.writeLine(formatString + separator);
        }
    }

    /**
     * Saves the format string in the quotes into the variable.
     *
     * @param format the format node
     */
    @Override
    public void visit(Format format) {
        formatString = '"' + format.getFormatString() + '"';
    }

}
