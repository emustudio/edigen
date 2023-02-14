/*
 * This file is part of edigen.
 *
 * Copyright (C) 2011-2023 Matúš Sulír, Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

/**
 * A visitor which pushes the variant nodes down to the bottom of the tree.
 * <p>
 * Expectation of a tree at input, e.g.:
 * <pre>
 *   Rule
 *     Variant
 *       Mask
 *         Pattern
 *           ...
 *             Mask
 *               Pattern
 * </pre>
 * <p>
 * Expectation of the tree at output:
 * <pre>
 *   Rule
 *     Mask
 *       Pattern
 *         ...
 *           Mask
 *             Pattern
 *               Variant
 * </pre>
 */
public class PushDownVariantsVisitor extends Visitor {

    private Variant currentVariant;
    private Mask topMask;

    /**
     * Attaches the top mask of each variant to the rule.
     *
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
     * Saves the current variant and detaches the variant from the rule if at
     * least one variant's child is a mask.
     *
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
     *
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
     *
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
