/*
 * This file is part of edigen.
 *
 * Copyright (C) 2011-2023 Matúš Sulír, Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
     * @param decoder the decoder node
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
     * @return the decoder
     */
    public Decoder getDecoder() {
        return decoder;
    }

    /**
     * Returns the disassembler node.
     * @return the disassembler node
     */
    public Disassembler getDisassembler() {
        return disassembler;
    }

    /**
     * Accepts the visitor.
     * @param visitor the visitor object
     * @throws SemanticException depends on the specific visitor
     */
    @Override
    public void accept(Visitor visitor) throws SemanticException {
        visitor.visit(this);
    }

    /**
     * Returns a string representation of the object.
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
