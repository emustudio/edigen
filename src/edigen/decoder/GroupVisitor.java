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
import edigen.tree.Pattern;
import edigen.tree.Rule;
import edigen.tree.TreeNode;
import edigen.util.BitSequence;
import java.util.HashMap;
import java.util.Map;

/**
 * Groups sibling masks or patterns containing the same bit sequences into one
 * node.
 * 
 * All children of the original nodes will be attached to the grouped node.
 * @author Matúš Sulír
 */
public class GroupVisitor extends Visitor {
    
    private boolean savingBits;
    private BitSequence savedBits;
    
    /**
     * Groups the rule recursively.
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        group(rule);
    }

    /**
     * If the current task is to save bits, saves them and returns the control;
     * otherwise groups the mask's children and thus continues the traversal.
     * @param mask the mask node
     * @throws SemanticException never 
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        if (savingBits)
            savedBits = mask.getBits();
        else
            group(mask);
    }

    /**
     * If the current task is to save bits, saves them and returns the control;
     * otherwise groups the pattern's children and thus continues the traversal.
     * @param mask the pattern node
     * @throws SemanticException never 
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        if (savingBits)
            savedBits = pattern.getBits();
        else
            group(pattern);
    }
    
    /**
     * Groups the children nodes and continues the traversal.
     * @param node a rule, mask or pattern
     * @throws SemanticException never
     */
    private void group(TreeNode node) throws SemanticException {
        Map<BitSequence, TreeNode> uniqueChildren = new HashMap<BitSequence, TreeNode>();
        
        // group children
        for (TreeNode child : node.getChildren()) {
            // obtain the bit sequence
            savedBits = null;
            savingBits = true;
            child.accept(this);
            
            // add the node to the corresponding group
            if (savedBits != null) {
                TreeNode uniqueChild = uniqueChildren.get(savedBits);

                if (uniqueChild == null) {
                    uniqueChildren.put(savedBits, child);
                } else {
                    child.remove();
                    uniqueChild.addChild(child.getChild(0));
                }
            }
        }
        
        // traverse
        savingBits = false;
        node.acceptChildren(this);
    }
}
