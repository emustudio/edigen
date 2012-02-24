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
import edigen.decoder.tree.Variant;

/**
 * A visitor which moves the variant nodes to the bottom of the tree.
 * @author Matúš Sulír
 */
public class MoveVariantVisitor extends Visitor {

    private Variant currentVariant;
    private Mask topMask;

    /**
     * Attaches the top mask of each variant to the rule.
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        for (TreeNode child : rule.getChildren()) {
            child.accept(this);

            if (topMask != null)
                rule.addChild(topMask);
        }
    }

    /**
     * Saves the current variant and dettaches the variant from the rule if at
     * least one variant's child is a mask.
     * @param variant the variant node
     * @throws SemanticException never
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        currentVariant = variant;
        topMask = null;
        TreeNode parent = variant.getParent();
        
        variant.remove();
        variant.acceptChildren(this);
        
        if (topMask == null)
            parent.addChild(variant);
    }

    /**
     * Saves the topmost mask of the variant and dettaches it from the variant.
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        if (topMask == null) {
            topMask = mask;
            mask.remove();
        }

        mask.acceptChildren(this);
    }

    /**
     * Attaches the variant to the bottommost pattern.
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        if (pattern.childCount() == 0)
            pattern.addChild(currentVariant);
        else
            pattern.acceptChildren(this);
    }
}
