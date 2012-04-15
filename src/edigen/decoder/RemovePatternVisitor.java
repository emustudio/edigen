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
import edigen.Visitor;
import edigen.tree.Mask;
import edigen.tree.TreeNode;

/**
 * A visitor which removes patterns which are children of zero-only masks.
 * 
 * This represents the fact that masks containing only zeroes do not require
 * comparing with the pattern because the result is always true.
 * @author Matúš Sulír
 */
public class RemovePatternVisitor extends Visitor {

    /**
     * Removes the child pattern if the mask contains only zeroes.
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        if (mask.getBits().containsOnly(false)) {
            TreeNode pattern = mask.getChild(0);
            TreeNode childMask = pattern.getChild(0);
            
            pattern.remove();
            mask.addChild(childMask);
        }
        
        mask.acceptChildren(this);
    }
    
}
