/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.TreeNode;

/**
 * A visitor which removes patterns which are children of zero-only masks.
 * <p>
 * This represents the fact that masks containing only zeroes do not require
 * comparing with the pattern because the result is always true.
 */
public class RemoveUnreachablePatternsVisitor extends Visitor {

    /**
     * Constructs a new remove-unreachable-patterns visitor.
     */
    public RemoveUnreachablePatternsVisitor() {
    }

    /**
     * Removes the child pattern if the mask contains only zeroes.
     *
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
