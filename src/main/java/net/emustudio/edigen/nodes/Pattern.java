/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
     *
     * @param bits the bit sequence
     */
    public Pattern(BitSequence bits) {
        this.bits = bits;
    }

    /**
     * Returns the bit sequence.
     *
     * @return the bit sequence
     */
    public BitSequence getBits() {
        return this.bits;
    }

    /**
     * Returns a pattern ANDed with the specified mask.
     *
     * @param mask the mask
     * @return the resulting pattern
     */
    public Pattern and(Mask mask) {
        return new Pattern(bits.and(mask.getBits()));
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
     * Returns the pattern as a string in binary notation.
     *
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
