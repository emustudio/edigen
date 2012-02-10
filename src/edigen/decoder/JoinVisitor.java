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

import edigen.decoder.tree.Mask;
import edigen.decoder.tree.Pattern;
import edigen.decoder.tree.Subrule;
import edigen.decoder.tree.Variant;
import edigen.util.BitSequence;

/**
 * A visitor which joins multiple patterns of a variant into one mask + pattern.
 * 
 * In addition, it sets starting offsets for subrules.
 * @author Matúš Sulír
 */
public class JoinVisitor extends Visitor {

    private BitSequence maskBits;
    private BitSequence patternBits;
    private Subrule returnSubrule;
    
    /**
     * Traverses all children nodes and adds one mask and pattern as a result
     * of joining.
     * @param variant the variant node
     */
    @Override
    public void visit(Variant variant) {
        maskBits = new BitSequence();
        patternBits = new BitSequence();
        returnSubrule = variant.getReturnSubrule();
        
        variant.acceptChildren(this);
        variant.removeMarked();
        
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
        
        pattern.markForRemoval();
    }
    
    /**
     * Sets the starting offset of the subrule.
     * 
     * <p>If the subrule has length specified, adds "false" bits to the mask
     * because these bits will not be checked against a pattern during
     * decoding.</p>
     * 
     * <p>If the subrule has the same name as the subrule which the parent
     * variant returns, it is assocated with the variant and removed from the
     * tree.</p>
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
        
        if (returnSubrule != null && subrule.getName().equals(returnSubrule.getName())) {
            ((Variant) subrule.getParent()).setReturnSubrule(subrule);
            subrule.markForRemoval();
        }
    }
}
