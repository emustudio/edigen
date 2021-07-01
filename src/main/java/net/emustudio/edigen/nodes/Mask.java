/*
 * Copyright (C) 2012 Matúš Sulír
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.BitSequence;

import java.util.Objects;

/**
 * Mask node - a sequence of bits used to filter another sequence during binary
 * pattern matching.
 */
public class Mask extends TreeNode {
    
    private final BitSequence bits;
    private Integer start;
    
    /**
     * Constructs the mask.
     * @param bits the bit sequence
     */
    public Mask(BitSequence bits) {
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
     * Returns the starting offset relative to the variant start.
     * 
     * The result is null if it is not yet determined.
     * @return the starting offset, in bits
     */
    public Integer getStart() {
        return start;
    }

    /**
     * Sets the starting offset relative to the variant start.
     * @param start the starting offset, in bits
     */
    public void setStart(int start) {
        this.start = start;
    }
    
    /**
     * Returns a mask ANDed with an another mask.
     * @param other the second mask
     * @return the resulting mask
     */
    public Mask and(Mask other) {
        return new Mask(bits.and(other.bits));
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
     * Returns the mask as a string in binary notation.
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Mask: " + bits.toString());

        if (start != null) {
            builder.append(", start: ").append(start);
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mask mask = (Mask) o;

        if (!Objects.equals(bits, mask.bits)) return false;
        return Objects.equals(start, mask.start);
    }

    @Override
    public int hashCode() {
        int result = bits != null ? bits.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        return result;
    }
}
