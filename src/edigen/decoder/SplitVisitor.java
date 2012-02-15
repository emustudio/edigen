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
package edigen.decoder;

import edigen.SemanticException;
import edigen.decoder.tree.Mask;
import edigen.decoder.tree.Pattern;
import edigen.decoder.tree.Variant;
import edigen.util.BitSequence;

/**
 * A visitor which splits the patterns and mask into smaller pieces of the same
 * length.
 * 
 * This is necessary to support instructions with variable length, especially
 * instructions with length larger than <code>int</code> or <code>long</code>
 * size.
 * @author Matúš Sulír
 */
public class SplitVisitor extends Visitor {

    private static final int BYTES_PER_PIECE = 1;
    
    private BitSequence maskBits;
    private BitSequence patternBits;
    
    /**
     * Splits the mask and pattern and adds 
     * @param variant 
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        variant.acceptChildren(this);
        variant.removeMarked();
        
        BitSequence[] masks = maskBits.split(BYTES_PER_PIECE);
        BitSequence[] patterns = patternBits.split(BYTES_PER_PIECE);
        
        TreeNode parent = variant;
        
        for (int i = 0; i < masks.length; i++) {
            Mask mask = new Mask(masks[i]);
            Pattern pattern = new Pattern(patterns[i]);
            
            parent.addChild(mask);
            mask.addChild(pattern);
            
            parent = pattern;
        }
    }

    /**
     * Saves the mask bits and marks the node for removal.
     * @param mask the mask node
     */
    @Override
    public void visit(Mask mask) {
        maskBits = mask.getBits();
        mask.markForRemoval();
    }

    /**
     * Saves the pattern bits and marks the node for removal.
     * @param pattern the pattern node
     */
    @Override
    public void visit(Pattern pattern) {
        patternBits = pattern.getBits();
        pattern.markForRemoval();
    }
}
