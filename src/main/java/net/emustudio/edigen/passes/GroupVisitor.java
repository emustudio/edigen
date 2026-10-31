/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Pattern;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.TreeNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Groups sibling masks or patterns containing the same bit sequences into one
 * node.
 * <p>
 * All children of the original nodes will be attached to the grouped node.
 * <p>
 * Expectation of a tree at input, e.g:
 * <pre>
 *   Rule
 *     Mask (111)
 *       Pattern (110)
 *         Variant
 *     Mask (111)
 *       Pattern (111)
 *         Variant
 * </pre>
 * <p>
 * Expectation of the tree at output:
 * <pre>
 *   Rule
 *     Mask (111)
 *       Pattern (110)
 *         Variant
 *       Pattern (111)
 *         Variant
 * </pre>
 */
public class GroupVisitor extends Visitor {

    /**
     * Constructs a new group visitor.
     */
    public GroupVisitor() {
    }

    /**
     * Groups the rule recursively.
     *
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        group(rule);
    }

    /**
     * Groups the mask's children and continues the traversal.
     *
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        group(mask);
    }

    /**
     * Groups the pattern's children and continues the traversal.
     *
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        group(pattern);
    }

    /**
     * Groups the children nodes and continues the traversal.
     *
     * @param node a rule, mask or pattern
     * @throws SemanticException never
     */
    private void group(TreeNode node) throws SemanticException {
        Map<BitSequence, TreeNode> uniqueChildren = new LinkedHashMap<>();

        // group children
        for (TreeNode child : node.getChildren()) {
            BitSequence bits = bitsOf(child);

            // add the node to the corresponding group
            if (bits != null) {
                TreeNode uniqueChild = uniqueChildren.get(bits);

                if (uniqueChild == null) {
                    uniqueChildren.put(bits, child);
                } else {
                    child.remove();
                    uniqueChild.addChild(child.getChild(0));
                }
            }
        }

        // traverse
        node.acceptChildren(this);
    }

    private static BitSequence bitsOf(TreeNode node) {
        if (node instanceof Mask) {
            return ((Mask) node).getBits();
        }
        if (node instanceof Pattern) {
            return ((Pattern) node).getBits();
        }
        return null;
    }
}
