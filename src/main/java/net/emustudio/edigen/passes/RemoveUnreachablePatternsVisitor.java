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
