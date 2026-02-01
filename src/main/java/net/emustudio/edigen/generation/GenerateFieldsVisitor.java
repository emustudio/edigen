/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.Variant;

import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A visitor which generates Java source code of the instruction decoder fields
 * for rules and values.
 * <p>
 * Each rule (which has at least one returning variant) and string-returning
 * variant is given a unique integral constant which can be later used in a
 * disassembler and emulator.
 */
public class GenerateFieldsVisitor extends Visitor {

    private final PrettyPrinter printer;
    private boolean ruleReturns;
    private final Set<String> fields = new LinkedHashSet<>();

    /**
     * Constructs the visitor.
     *
     * @param writer the output stream to write the code to
     */
    public GenerateFieldsVisitor(Writer writer) {
        this.printer = new PrettyPrinter(writer);
    }

    /**
     * Writes the constants.
     *
     * @param decoder the decoder node
     * @throws SemanticException never
     */
    @Override
    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);
        int ruleNumber = 1;

        for (String field : fields) {
            printer.writeLine("public static final int "
                    + field + " = " + ruleNumber++ + ";");
        }
    }

    /**
     * Adds the field names for the particular rule to the list.
     *
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        ruleReturns = false;
        rule.acceptChildren(this);

        if (ruleReturns) {
            for (String name : rule.getNames()) {
                fields.add(rule.getFieldName(name));
            }
        }
    }

    /**
     * Adds the field to the list and sets the flag if the variant returns
     * something.
     *
     * @param variant the variant node
     */
    @Override
    public void visit(Variant variant) {
        if (variant.getFieldName() != null)
            fields.add(variant.getFieldName());

        if (variant.returns())
            ruleReturns = true;
    }

}
