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
package edigen.decoder.tree;

import edigen.SemanticException;
import edigen.decoder.TreeNode;
import edigen.decoder.Visitor;
import edigen.util.BitSequence;

/**
 * Pattern node - a sequence of bits used during instruction decoding.
 * @author Matúš Sulír
 */
public class Pattern extends TreeNode {
    
    private BitSequence bits;
    
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
     * Accepts the visitor.
     * @param visitor the visitor object
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
}
