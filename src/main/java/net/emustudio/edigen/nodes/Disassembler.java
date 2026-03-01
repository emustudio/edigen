/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;

/**
 * The root node of the disassembler subtree.
 */
public class Disassembler extends TreeNode {

    /**
     * Constructs a new disassembler node.
     */
    public Disassembler() {
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
        return "Disassembler";
    }

    @Override
    public TreeNode shallowCopy() {
        return new Disassembler();
    }
}
