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
 * A visitor which checks for ambiguous variants.
 * <p>
 * There are two types of ambiguity.
 * <p>
 * Path ambiguity means that for some rule, given a unit of input (usually a
 * byte), the decoder is not able to decide which one (and only one) execution
 * path to choose without reading more units or walking up the switch-case tree
 * later and choosing an another path (or none at all). Ambiguity is dangerous
 * because it can cause unintended behavior not discovered until the generated
 * decoder is run.
 * <p>
 * Two variants of one rule R have an ambiguous path if and only if there
 * exists a node N of type Rule (equivalent to R) or Pattern (an indirect child
 * of R) such that:
 * <ul>
 *  <li>There exist two Mask nodes M1, M2, both direct children of N, such that:
 *  <ul>
 *   <li>There exist two Pattern nodes P1, P2, where P1 is a direct child of M1
 *       and P2 is a direct child of M2, such that: M1 &amp; M2 &amp; P1 = M1 &amp; M2 &amp; P2.
 *   </li>
 *  </ul>
 *  </li>
 * </ul>
 * <p>
 * Variant ambiguity means that one pattern has more than one child variant.
 * <p>
 * This visitor is not a transformation; the tree is left unmodified. It
 * must be applied after the grouping transformation.
 *
 */
public class DetectAmbiguousVisitor extends Visitor {

    private static final String MESSAGE = "Ambiguity detected in rule \"%s\"";
    private Rule currentRule;
    private final List<Mask> masks = new ArrayList<Mask>();

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
        traverseSubtrees(rule);
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
        traverseSubtrees(pattern);
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
    private void traverseSubtrees(TreeNode node) throws SemanticException {
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
            throw new SemanticException(String.format(MESSAGE, currentRule.getLabel())
                    + ": " + pattern.toString(), currentRule);
        }
    }
}
