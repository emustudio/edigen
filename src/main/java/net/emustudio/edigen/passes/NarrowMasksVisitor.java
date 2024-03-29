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
import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Pattern;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.TreeNode;

/**
 * A visitor which removes all child masks of a node except the first one,
 * attaches them to a new empty pattern and attaches that pattern (if it is not
 * empty) to the first mask.
 * <p>
 * This is necessary to represent the fact that if no pattern matches an input,
 * the next mask (and its associated patterns) is tried.
 */
public class NarrowMasksVisitor extends Visitor {

    /**
     * Moves all child masks of the rule node except the first one.
     *
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        narrowMasks(rule);
    }

    /**
     * Moves all child masks of the pattern node except the first one.
     *
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        narrowMasks(pattern);
    }

    /**
     * Removes all child masks of the given node except the first one, attaches
     * them to a new empty pattern and attaches that pattern (if it is not
     * empty) to the first mask.
     * <p>
     * One node can contain multiple child masks also as a result of previous
     * moving. By recursive application, it is ensured that each node will
     * contain only one child mask.
     *
     * @param node the node object
     * @throws SemanticException never
     */
    private void narrowMasks(TreeNode node) throws SemanticException {
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
