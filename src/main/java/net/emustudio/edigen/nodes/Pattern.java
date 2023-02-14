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
import net.emustudio.edigen.misc.BitSequence;

import java.util.Objects;

/**
 * Pattern node - a sequence of bits used during instruction decoding.
 */
public class Pattern extends TreeNode {

    private final BitSequence bits;

    /**
     * Constructs a pattern.
     * @param bits the bit sequence
     */
    public Pattern(BitSequence bits) {
        this.bits = bits;
    }

    /**
     * Returns the bit sequence.
     * @return the bit sequence
     */
    public BitSequence getBits() {
        return this.bits;
    }

    /**
     * Returns a pattern ANDed with the specified mask.
     * @param mask the mask
     * @return the resulting pattern
     */
    public Pattern and(Mask mask) {
        return new Pattern(bits.and(mask.getBits()));
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
     * Returns the pattern as a string in binary notation.
     * @return the string
     */
    @Override
    public String toString() {
        return "Pattern: " + bits.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern = (Pattern) o;

        return Objects.equals(bits, pattern.bits);
    }

    @Override
    public int hashCode() {
        return bits != null ? bits.hashCode() : 0;
    }

    @Override
    public TreeNode shallowCopy() {
        return new Pattern(bits);
    }
}
