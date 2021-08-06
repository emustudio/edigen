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
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Pattern;
import net.emustudio.edigen.nodes.TreeNode;
import net.emustudio.edigen.nodes.Variant;

/**
 * A visitor which splits the patterns and mask into smaller pieces of the same
 * length.
 * 
 * This is necessary to support instructions with variable length, especially
 * instructions with length larger than <code>int</code> or <code>long</code>
 * size.
 *
 * Expectation of a tree at input, e.g.:
 * <pre>
 *   Rule
 *     Variant
 *     Mask (length &gt; BITS_PER_PIECE)
 *     Pattern (length &gt; BITS_PER_PIECE)
 * </pre>
 *
 * Expectation of the tree at output:
 * <pre>
 *   Rule
 *     Variant
 *       Mask (length = BITS_PER_PIECE)
 *         Pattern (length = BITS_PER_PIECE)
 *           ...
 *             Mask (length &lt;= BITS_PER_PIECE)
 *               Pattern (length &lt;= BITS_PER_PIECE)
 * </pre>
 */
public class SplitVisitor extends Visitor {

    public static final int BITS_PER_PIECE = 32;
    
    private BitSequence maskBits;
    private BitSequence patternBits;
    
    /**
     * Splits the mask and pattern and adds the split pieces to the variant.
     * 
     * <p>The nodes are placed "vertically" - each pattern is a child of the
     * corresponding mask and each mask is a child of the previous pattern
     * (or variant, if there is no previous pattern).</p>
     * 
     * <p>In addition, starting positions of the masks are set.</p>
     * @param variant the variant node
     * @throws SemanticException never
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        variant.acceptChildren(this);
        
        BitSequence[] masks = maskBits.split(BITS_PER_PIECE);
        BitSequence[] patterns = patternBits.split(BITS_PER_PIECE);
        
        TreeNode parent = variant;
        
        for (int i = 0; i < masks.length; i++) {
            Mask mask = new Mask(masks[i]);
            mask.setStart(i * BITS_PER_PIECE);
            
            Pattern pattern = new Pattern(patterns[i]);
            
            parent.addChild(mask);
            mask.addChild(pattern);
            
            parent = pattern;
        }
    }

    /**
     * Saves the mask bits and removes the node.
     * @param mask the mask node
     */
    @Override
    public void visit(Mask mask) {
        maskBits = mask.getBits();
        mask.remove();
    }

    /**
     * Saves the pattern bits and removes the node.
     * @param pattern the pattern node
     */
    @Override
    public void visit(Pattern pattern) {
        patternBits = pattern.getBits();
        pattern.remove();
    }
}
