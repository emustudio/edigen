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
import edigen.decoder.tree.Rule;

/**
 * A visitor which removes all except the first sibling masks and attaches them
 * to the first mask.
 * 
 * This is necessary to represent the fact that if no pattern matches an input,
 * the next mask (an its associated patterns) is tried.
 * @author Matúš Sulír
 */
public class MoveMaskVisitor extends Visitor {

    /**
     * Moves all child masks of the rule node except the first one.
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        moveMasks(rule);
    }

    /**
     * Moves all child masks of an another mask node except the first one.
     * 
     * One mask can contain multiple child masks as a result of previous moving.
     * By recursive application of this method, it is ensured that each node
     * will contain only one child mask.
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        moveMasks(mask);
    }
    
    /**
     * Moves all child masks of the pattern node except the first one.
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        moveMasks(pattern);
    }
    
    /**
     * Removes all child masks of the given node except the first one and
     * attaches them to the first child mask.
     * @param node the node object
     * @throws SemanticException never
     */
    private void moveMasks(TreeNode node) throws SemanticException {
        TreeNode firstMask = null;

        for (TreeNode child : node.getChildren()) {
            if (child instanceof Mask) {
                if (firstMask == null) {
                    firstMask = child;
                } else {
                    child.remove();
                    firstMask.addChild(child);
                }
            }
        }

        node.acceptChildren(this);
    }
}
