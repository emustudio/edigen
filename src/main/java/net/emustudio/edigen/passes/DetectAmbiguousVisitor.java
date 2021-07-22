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
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Pattern;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A visitor which checks for ambiguous variants.
 * This visitor is not a transformation; the tree is left unmodified. It
 * must be applied after the grouping transformation.
 * </p>
 *
 * <p>
 * Ambiguity is dangerous, because it can cause unintended behavior not discovered until the generated decoder is run.
 * There are two types of ambiguity: path ambiguity and variant ambiguity.
 * </p>
 *
 * <h3>Path ambiguity</h3>
 * <p>
 * Path ambiguity means that for some rule and input, decoder is not able to decide which execution path to choose
 * without reading more input, even if the deep contains solution. The reason is that Decoder is simple state machine
 * which decides only based on the current state and input. It means that the current implementation of decoder doesn't
 * support look-aheads.
 * </p>
 * <p>
 * Effect of path ambiguity is that only the first found path would be executed by decoder, and all other ambiguous paths
 * would be ignored forever. Therefore this smells of unintended behavior.
 * </p>
 * <p>
 * Let N be a rule node or a Pattern node which is an indirect child of the rule. Then, two variants of the rule have
 * ambiguous path if:
 * <ul>
 *     <li>N has two direct children: masks M1 and M2</li>
 *     <li>M1 has direct child pattern P1</li>
 *     <li>M2 has direct child pattern P2</li>
 *     <li><code>M1 &amp; M2 &amp; P1 = M1 &amp; M2 &amp; P2</code></li>
 * </ul>
 *
 * Example of path ambiguity:
 * <pre>
 *     Rule
 *       Mask (111)
 *         Pattern (101)
 *       Mask (101)
 *         Pattern (101)
 * </pre>
 *
 * <h3>Variant ambiguity</h3>
 * Variant ambiguity means that one pattern has more than one child variant.
 *
 */
public class DetectAmbiguousVisitor extends Visitor {

    private static final String MESSAGE = "Ambiguity detected in rule \"%s\"";
    private Rule currentRule;
    private final List<Mask> masks = new ArrayList<>();

    /**
     * Detects possible ambiguity under the rule node and traverses the
     * children.
     *
     * @param rule the rule node
     * @throws SemanticException when an ambiguity is detected
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        currentRule = rule;
        detectPath(rule);
        traverseChildrenSubtrees(rule); // direct children are masks
    }

    /**
     * Adds the mask to the list of child masks.
     *
     * @param mask the mask node
     */
    @Override
    public void visit(Mask mask) {
        masks.add(mask);
    }

    /**
     * Detects possible ambiguity under the pattern node and traverses the
     * children.
     *
     * @param pattern the pattern node
     * @throws SemanticException when an ambiguity is detected
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        detectVariant(pattern);
        detectPath(pattern);
        traverseChildrenSubtrees(pattern); // direct children are masks
    }

    /**
     * Detects the path ambiguity under the node specified.
     * <p>
     * All combinations without repetition of the child masks are tried. For all
     * of these, all pattern pairs made of the first mask's children and the
     * second mask's children are checked.
     *
     * @param node the node object
     * @throws SemanticException when the path ambiguity is detected
     */
    private void detectPath(TreeNode node) throws SemanticException {
        masks.clear();
        node.acceptChildren(this);

        int maskCount = masks.size();
        int firstMaskIndex = 0;

        for (Mask mask1 : masks) {
            for (Mask mask2 : masks.subList(++firstMaskIndex, maskCount)) {
                Mask commonMask = mask1.and(mask2);

                for (TreeNode pattern1 : mask1.getChildren()) {
                    for (TreeNode pattern2 : mask2.getChildren()) {
                        if (isAmbiguous((Pattern) pattern1, (Pattern) pattern2, commonMask)) {
                            String message = String.format(MESSAGE, currentRule.getLabel())
                                    + ": " + pattern1 + ", " + pattern2 + " (" + commonMask + ")";
                            throw new SemanticException(message, currentRule);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true if two patterns are ambiguous.
     *
     * @param pattern1   the first pattern
     * @param pattern2   the second pattern
     * @param commonMask the first mask ANDed with the second mask
     * @return true if the patterns are ambiguous, false otherwise
     */
    private boolean isAmbiguous(Pattern pattern1, Pattern pattern2, Mask commonMask) {
        BitSequence commonPattern1 = pattern1.and(commonMask).getBits();
        BitSequence commonPattern2 = pattern2.and(commonMask).getBits();

        return (commonPattern1.equals(commonPattern2));
    }

    /**
     * Accepts all children of the children of the given node.
     *
     * @param node the parent node
     * @throws SemanticException when an ambiguity is detected
     */
    private void traverseChildrenSubtrees(TreeNode node) throws SemanticException {
        for (TreeNode child : node.getChildren()) {
            for (TreeNode subchild : child.getChildren()) {
                subchild.accept(this);
            }
        }
    }

    /**
     * Detects the variant ambiguity under the specified pattern.
     *
     * @param pattern the pattern node
     * @throws SemanticException when the variant ambiguity is detected
     */
    private void detectVariant(Pattern pattern) throws SemanticException {
        if (pattern.childCount() > 1) {
            throw new SemanticException(String.format(MESSAGE, currentRule.getLabel()) + ": " + pattern, currentRule);
        }
    }
}
