/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;

/**
 * A node representing a textual instruction format (used in a disassembler).
 * <p>
 * Consists of a format string and a list of values.
 */
public class Format extends TreeNode {

    private final String formatString;

    /**
     * Constructs the format node.
     *
     * @param formatString the format string
     */
    public Format(String formatString) {
        this.formatString = formatString;
    }

    /**
     * Returns the format string.
     *
     * @return the string
     */
    public String getFormatString() {
        return formatString;
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor the visitor object
     * @throws SemanticException depends on the specific visitor
     */
    @Override
    public void accept(Visitor visitor) throws SemanticException {
        visitor.visit(this);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Format: \"" + formatString + '"';
    }

    @Override
    public TreeNode shallowCopy() {
        return new Format(formatString);
    }
}
