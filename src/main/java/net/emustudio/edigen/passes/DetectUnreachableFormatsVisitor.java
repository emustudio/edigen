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

import java.util.*;

/**
 * Detects unreachable disassembler formats.
 * <p>
 * root instruction;
 * instruction =
 * "nop": 00000000 |
 * other
 * ;
 * <p>
 * other =
 * "hey": 10000000 |
 * "arg %d": 10000001 arg
 * ;
 * <p>
 * arg = arg: arg(8);
 * <p>
 * %%
 * <p>
 * "%s" = instruction arg;    // unreachable
 */
public class DetectUnreachableFormatsVisitor extends Visitor {
    private final Set<Set<String>> reachable = new HashSet<>();
    private final Set<Set<String>> formats = new HashSet<>();
    private Set<String> currentFormat;

    public Set<Set<String>> getReachable() {
        return new HashSet<>(reachable);
    }

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        BuildSlimTreeVisitor slimTreeVisitor = new BuildSlimTreeVisitor();
        for (Rule rule : decoder.getRootRules()) {
            rule.accept(slimTreeVisitor);
        }

        List<Rule> slimTree = slimTreeVisitor.slimTree;
        List<Visitor> additionalVisitors = List.of(
                new RemoveOrphanSubrulesVisitor(),
                new UniqueSubrulePathsVisitor(),
                new EliminateVariantsVisitor()
        );

        for (Rule rule : slimTree) {
            for (Visitor visitor : additionalVisitors) {
                rule.accept(visitor);
            }
            CollectPathsVisitor collectVisitor = new CollectPathsVisitor();
            rule.accept(collectVisitor);
            reachable.addAll(collectVisitor.allPaths);
        }
    }

    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        disassembler.acceptChildren(this);

        // detects unreachable formats (missing formats are ok)
        if (!reachable.containsAll(formats)) {
            Set<Set<String>> cp = new HashSet<>(formats);
            cp.removeAll(reachable);
            throw new SemanticException("Unreachable formats: " + cp, disassembler);
        }
        if (!formats.containsAll(reachable)) {
            Set<Set<String>> cp = new HashSet<>(reachable);
            cp.removeAll(formats);
            System.out.println("Missing formats: " + cp);
        }
    }

    @Override
    public void visit(Format format) throws SemanticException {
        currentFormat = new HashSet<>();
        format.acceptChildren(this);
        if (!currentFormat.isEmpty()) {
            formats.add(currentFormat);
        }
    }

    @Override
    public void visit(Value value) throws SemanticException {
        currentFormat.add(value.getName());
    }

    /**
     * Creates a copy of current tree:
     * - preserves just root rules, variants and subrules.
     * - expands subrules - adds subule rule "pointers" as children.
     * - ignores masks and patterns.
     */
    private static class BuildSlimTreeVisitor extends Visitor {
        final List<Rule> slimTree = new ArrayList<>();
        private TreeNode current;

        @Override
        public void visit(Rule rule) throws SemanticException {
            Rule newCurrent = new Rule(rule.getNames());
            if (rule.getRootRuleName() != null) {
                newCurrent.setRoot(rule.isRoot(), rule.getRootRuleName());
            }
            if (rule.isRoot()) {
                slimTree.add(newCurrent);
            }
            current = newCurrent;
            rule.acceptChildren(this);
            current = newCurrent;
        }

        @Override
        public void visit(Variant variant) throws SemanticException {
            TreeNode old = current;

            Variant newCurrent = new Variant();
            if (variant.getReturnString() != null) {
                newCurrent.setReturnString(variant.getReturnString());
            } else if (variant.getReturnSubrule() != null) {
                Subrule newSubrule = new Subrule(variant.getReturnSubrule().getName());
                if (variant.getReturnSubrule().getRule() != null) {
                    variant.getReturnSubrule().getRule().accept(this);
                    newSubrule.setRule((Rule) current);
                    current = old;
                }
                newCurrent.setReturnSubrule(newSubrule);
            }
            current.addChild(newCurrent);
            current = newCurrent;

            variant.acceptChildren(this);
            current = old;
        }

        @Override
        public void visit(Subrule subrule) throws SemanticException {
            TreeNode old = current;

            // subrule has no children, except rule
            Subrule newCurrent = new Subrule(subrule.getName());
            current.addChild(newCurrent);
            if (subrule.getRule() != null) {
                current = newCurrent;
                subrule.getRule().acceptChildren(this);
                current = old;
            }
        }
    }

    /**
     * Preserves only subrules used as the format key; removes empty "pointers" to itself.
     * Subrules with no children do not add the format key (variants do).
     */
    private static class RemoveOrphanSubrulesVisitor extends Visitor {

        @Override
        public void visit(Subrule subrule) throws SemanticException {
            if (subrule.childCount() == 0) {
                subrule.remove();
            } else {
                subrule.acceptChildren(this);
            }
        }
    }

    /**
     * Transform a tree so each reachable path is a full path, so no siblings need to be considered anymore.
     * It means: all siblings are put under all reachable paths of the first child; recursively bottom up.
     * Generally, it is one possible implementation of generating combinations.
     * <p>
     * Rule A
     * Variant
     * Subrule B
     * Variant
     * Subrule E
     * Variant (return "aa")
     * Subrule C
     * Subrule D
     * Subrule F
     * <p>
     * Result:
     * Rule A
     * Variant
     * Subrule B
     * Variant
     * Subrule E
     * Subrule F
     * Variant (return "aa")
     * Subrule C
     * Subrule D
     * Subrule F
     */
    private static class UniqueSubrulePathsVisitor extends Visitor {

        @Override
        public void visit(Variant variant) throws SemanticException {
            variant.acceptChildren(this);

            // prepare subrule children on a side
            List<Subrule> children = new ArrayList<>();
            variant.getChildren().forEach(t -> children.add((Subrule) t));

            if (!children.isEmpty()) {
                Subrule firstChild = children.get(0);
                children.remove(0);

                // add siblings to the first child
                for (Subrule child : children) {
                    child.remove();
                    addRecursively(firstChild, child);
                }
            }
        }

        private void addRecursively(TreeNode where, TreeNode what) {
            if (where.childCount() == 0) {
                where.addChild(what.copy());
            } else {
                for (TreeNode child : where.getChildren()) {
                    addRecursively(child, what);
                }
            }
        }
    }


    /**
     * Tough logic of eliminating variants. Tough, because variants can but don't have to return.
     * <p>
     * Returning variants peculiarities:
     * - if the parent is rule, we must add artificial subrule and add variant's children to it before removing the
     * variant (otherwise rule "A" wont be recognized)
     * <p>
     * Rule A                        Rule A
     * Variant (return "a")   ->     Subrule A
     * ...                           ...
     * <p>
     * - if variant has no children, instead just removing variant we must replace it with artificial subrule
     * <p>
     * Subrule C                         Subrule D
     * Variant                           Subrule D
     * Subrule D                ->   Subrule C
     * Variant (return "d")
     * Variant (return "c")
     * <p>
     * Non-returning variants peculiarities:
     * - non-returning variants and it's parent subrules must be eliminated when the parent has only one child
     * (this variant)
     * <p>
     * Rule A                            Rule A
     * Subrule B                         Subrule C
     * Variant                   ->
     * Subrule C
     * Variant (return "c")
     * <p>
     * - if parent of non-returning variant has more children, we must keep it:
     * <p>
     * Rule A                            Rule A
     * Subrule B                         Subrule C
     * Variant                         Subrule B
     * Subrule C               ->      Subrule B
     * Variant (return "c")
     * Variant (return "b")
     */
    private static class EliminateVariantsVisitor extends Visitor {

        @Override
        public void visit(Variant variant) throws SemanticException {
            variant.acceptChildren(this);

            TreeNode parent = variant.getParent();
            List<TreeNode> children = variant.getChildren();
            children.forEach(TreeNode::remove);
            if (!variant.returns() && parent != null) {
                TreeNode parentParent = parent.getParent();
                if (parentParent != null) {
                    parentParent.addChildren(children);
                    if (parent.childCount() == 1) {
                        parent.remove();
                    }
                } else {
                    parent.addChildren(children);
                }
            } else if (parent != null) {
                // variant returns
                if (parent instanceof Rule) {
                    Subrule artificial = new Subrule(((Rule) parent).getNames().get(0));
                    artificial.addChildren(children);
                    parent.addChild(artificial);
                } else {
                    if (children.isEmpty()) {
                        Subrule artificial = new Subrule(((Subrule) parent).getName());
                        parent.addChild(artificial);
                    } else {
                        parent.addChildren(children);
                    }
                }
            }
            variant.remove();
        }
    }

    /**
     * Collects all unique paths.
     */
    private static class CollectPathsVisitor extends Visitor {
        final Set<Set<String>> allPaths = new HashSet<>();
        private Set<String> currentPath = new HashSet<>();

        @Override
        public void visit(Subrule subrule) throws SemanticException {
            Set<String> old = new HashSet<>(currentPath);
            currentPath.add(subrule.getName());

            if (subrule.childCount() == 0) {
                allPaths.add(currentPath);
            } else {
                subrule.acceptChildren(this);
            }
            currentPath = old;
        }
    }
}
