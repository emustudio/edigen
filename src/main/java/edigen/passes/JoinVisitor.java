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
package edigen.passes;

import edigen.SemanticException;
import edigen.Visitor;
import edigen.misc.BitSequence;
import edigen.nodes.Mask;
import edigen.nodes.Pattern;
import edigen.nodes.Subrule;
import edigen.nodes.Variant;

/**
 * A visitor which joins multiple patterns of a variant into one mask + pattern.
 * 
 * In addition, it sets starting offsets for subrules.
 * @author Matúš Sulír
 */
public class JoinVisitor extends Visitor {

    private BitSequence maskBits;
    private BitSequence patternBits;
    
    /**
     * Traverses all children nodes and adds one mask and pattern as a result
     * of joining.
     * @param variant the variant node
     * @throws SemanticException never
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        maskBits = new BitSequence();
        patternBits = new BitSequence();
        
        variant.acceptChildren(this);
        
        variant.addChild(new Mask(maskBits));
        variant.addChild(new Pattern(patternBits));
    }

    /**
     * Appends "true" bits to the mask (because these bits will be checked
     * during decoding) and itself to the pattern (these bits should be the
     * result of masking during decoding).
     * @param pattern the pattern node
     */
    @Override
    public void visit(Pattern pattern) {
        int bitCount = pattern.getBits().getLength();
        
        maskBits.append(new BitSequence(bitCount, true));
        patternBits.append(pattern.getBits());
        
        pattern.remove();
    }
    
    /**
     * Sets the starting offset of the subrule.
     * 
     * <p>If the subrule has length specified, adds "false" bits to the mask
     * because these bits will not be checked against a pattern during
     * decoding.</p>
     * 
     * <p>If the subrule does not refer to a rule (it is used only to return
     * a value), it is removed from the tree.</p>
     * @param subrule the subrule node
     */
    @Override
    public void visit(Subrule subrule) {
        subrule.setStart(maskBits.getLength());
        
        Integer bitCount = subrule.getLength();
        
        if (bitCount != null) {
            maskBits.append(new BitSequence(bitCount, false));
            patternBits.append(new BitSequence(bitCount, false));
        }
        
        if (subrule.getRule() == null)
            subrule.remove();
    }
}