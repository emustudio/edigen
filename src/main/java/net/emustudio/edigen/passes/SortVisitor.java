/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.TreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A visitor sorting the variants by the lengths of their masks - from
 * the shortest to the longest one.
 * <p>
 * This ensures that the generated decoder will not read bytes beyond
 * the current instruction.
 * <p>
 * Expected tree at input:
 * <pre>
 *   Rule
 *     Variant
 *       ...
 *       Mask (length=3)
 *       Pattern
 *     Variant
 *       ...
 *       Mask (length=4)
 *       Pattern
 * </pre>
 * <p>
 * Expected tree at output:
 * <pre>
 *   Rule
 *     Variant
 *       ...
 *       Mask (length=4)
 *       Pattern
 *     Variant
 *       ...
 *       Mask (length=3)
 *       Pattern
 * </pre>
 */
public class SortVisitor extends Visitor {

    /**
     * Constructs a new sort visitor.
     */
    public SortVisitor() {
    }

    private final List<Mask> masks = new ArrayList<>();
    private final Comparator<Mask> byLength = Comparator.comparingInt((Mask mask) -> mask.getBits().getLength());

    /**
     * Collects the variants, sorts them by mask length and re-attaches them
     * in the correct order.
     *
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
     *
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        masks.add(mask);
        mask.getParent().remove();
    }
}
