/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;

/**
 * The root node of the specification AST.
 */
public class Specification extends TreeNode {

    private final Decoder decoder;
    private final Disassembler disassembler;

    /**
     * Constructs the specification node.
     *
     * @param decoder      the decoder node
     * @param disassembler the disassembler node
     */
    public Specification(Decoder decoder, Disassembler disassembler) {
        this.decoder = decoder;
        this.disassembler = disassembler;

        addChild(decoder);
        addChild(disassembler);
    }

    /**
     * Returns the instruction decoder node.
     *
     * @return the decoder
     */
    public Decoder getDecoder() {
        return decoder;
    }

    /**
     * Returns the disassembler node.
     *
     * @return the disassembler node
     */
    public Disassembler getDisassembler() {
        return disassembler;
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
        return "Specification";
    }

    @Override
    public TreeNode shallowCopy() {
        return new Specification(decoder, disassembler);
    }
}
