/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Disassembler;
import net.emustudio.edigen.nodes.Format;
import net.emustudio.edigen.nodes.TreeNode;
import net.emustudio.edigen.nodes.Value;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * A visitor which generates the code of the two-dimensional array of
 * disassembler parameters (i.e., values on the right side of a format).
 */
public class GenerateParametersVisitor extends Visitor {

    private final PrintWriter writer;

    /**
     * Constructs the visitor.
     *
     * @param writer the output stream to write the code to
     */
    public GenerateParametersVisitor(Writer writer) {
        this.writer = new PrintWriter(writer, true);
    }

    /**
     * Writes the list of format sets separated by commas.
     *
     * @param disassembler the disassembler node
     * @throws SemanticException never
     */
    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        Iterator<TreeNode> formats = disassembler.getChildren().iterator();

        while (formats.hasNext()) {
            formats.next().accept(this);

            if (formats.hasNext())
                writer.println(",");
        }
    }

    /**
     * Writes the list of parameters separated by commas and enclosed in curly
     * brackets.
     *
     * @param format the format node
     * @throws SemanticException never
     */
    @Override
    public void visit(Format format) throws SemanticException {
        writer.print("{");
        Iterator<TreeNode> values = format.getChildren().iterator();

        while (values.hasNext()) {
            values.next().accept(this);

            if (values.hasNext()) {
                writer.println(",");
                writer.print(" ");
            }
        }

        writer.print("}");
    }

    /**
     * Writes the name of the field for the disassembler parameter.
     *
     * @param value the value node (the parameter)
     * @throws SemanticException never
     */
    @Override
    public void visit(Value value) throws SemanticException {
        String strategies = value.getStrategies().stream()
                .map(s -> "Strategy." + s)
                .collect(Collectors.joining(", "));
        writer.print("new Parameter(" + value.getFieldName()
                + ", new byte[]{" + strategies + "})");
    }
}
