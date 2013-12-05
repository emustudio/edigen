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
import edigen.nodes.Rule;
import edigen.nodes.TreeNode;

/**
 * A visitor which removes all child masks of a node except the first one,
 * attaches them to a new empty pattern and attaches that pattern (if it is not
 * empty) to the first mask.
 * 
 * This is necessary to represent the fact that if no pattern matches an input,
 * the next mask (an its associated patterns) is tried.
 * @author Matúš Sulír
 */
public class MoveMasksVisitor extends Visitor {

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
     * Moves all child masks of the pattern node except the first one.
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        moveMasks(pattern);
    }
    
    /**
     * Removes all child masks of the given node except the first one, attaches
     * them to a new empty pattern and attaches that pattern (if it is not
     * empty) to the first mask.
     * 
     * One node can contain multiple child masks also as a result of previous
     * moving. By recursive application, it is ensured that each node will
     * contain only one child mask.
     * @param node the node object
     * @throws SemanticException never
     */
    private void moveMasks(TreeNode node) throws SemanticException {
        TreeNode firstMask = null;
        Pattern defaultPattern = new Pattern(new BitSequence());
        
        for (TreeNode child : node.getChildren()) {
            if (child instanceof Mask) {
                if (firstMask == null) {
                    firstMask = child;
                } else {
                    child.remove();
                    defaultPattern.addChild(child);
                }
            }
        }
        
        if (defaultPattern.childCount() != 0 && firstMask != null)
            firstMask.addChild(defaultPattern);
        
        node.acceptChildren(this);
    }
}
