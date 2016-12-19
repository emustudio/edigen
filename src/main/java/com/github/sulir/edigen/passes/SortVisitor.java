/*
 * Copyright (C) 2016 Matúš Sulír
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
package com.github.sulir.edigen.passes;

import com.github.sulir.edigen.SemanticException;
import com.github.sulir.edigen.Visitor;
import com.github.sulir.edigen.nodes.Mask;
import com.github.sulir.edigen.nodes.Rule;
import com.github.sulir.edigen.nodes.TreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A visitor sorting the variants by the lengths of their masks - from
 * the shortest to the longest one.
 *
 * This ensures that the generated decoder will not read bytes beyond
 * the current instruction.
 * @author Matúš Sulír
 */
public class SortVisitor extends Visitor {

    private List<Mask> masks = new ArrayList<>();
    private Comparator<Mask> byLength = (Mask mask1, Mask mask2) ->
            ((Integer) mask1.getBits().getLength()).compareTo(mask2.getBits().getLength());

    /**
     * Collects the variants, sorts them by mask length and re-attaches them
     * in the correct order.
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        masks.clear();
        rule.acceptChildren(this);
        masks.sort(byLength);

        for (TreeNode mask : masks) {
            rule.addChild(mask.getParent());
        }
    }

    /**
     * Adds the mask to the sorting list and detaches the parent variant.
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        masks.add(mask);
        mask.getParent().remove();
    }
}
