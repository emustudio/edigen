package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.*;

/**
 * Detects unreachable disassembler formats.
 */
public class DetectUnreachableFormatsVisitor extends Visitor {
    private final Set<Set<String>> reachable = new HashSet<>();
    public final Set<Set<String>> formats = new HashSet<>();
    private Set<String> currentFormat;

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
            formats.removeAll(reachable);
            throw new SemanticException("Some disassembler formats are unreachable: " + formats, disassembler);
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
     *   - preserves just root rules, variants and subrules.
     *   - expands subrules - adds subule rule "pointers" as children.
     *   - ignores masks and patterns.
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
                    newSubrule.setRule((Rule)current);
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

        @Override
        public void visit(Variant variant) throws SemanticException {
            variant.acceptChildren(this);
            if (variant.returns() && variant.childCount() == 0) {
                variant.remove();
            }
        }
    }

    /**
     * Transform a tree so each reachable path is a full path, so no siblings need to be considered anymore.
     * It means: all siblings are put under all reachable paths of the first child; recursively bottom up.
     * Generally, it is one possible implementation of generating combinations.
     *
     *  Rule A
     *    Variant
     *      Subrule B                  : ?    <-- this should be eliminated for B - E (just "E" should exist)
     *        Variant                  : N/A
     *          Subrule E              : "E"
     *        Variant (return "aa")    : "B"
     *          Subrule C              : "C"
     *          Subrule D              : "D"
     *      Subrule F                  : "F"
     *
     * Result:
     *  Rule A
     *    Variant
     *      Subrule B
     *        Variant
     *          Subrule E
     *            Subrule F
     *        Variant (return "aa")
     *          Subrule C
     *            Subrule D
     *              Subrule F
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
                where.addChild(what);
            } else {
                for (TreeNode child : where.getChildren()) {
                    addRecursively(child, what);
                }
            }
        }
    }


    /**
     *  Rule A
     *    Variant
     *      Subrule B
     *        Variant
     *          Subrule E
     *            Subrule F
     *        Variant (return "aa")
     *          Subrule C
     *            Subrule D
     *              Subrule F
     *
     *   Rule A
     *     Subrule E
     *       Subrule F
     *     Subrule B
     *       Subrule C
     *         Subrule D
     *           Subrule F
     *
     */
    private static class EliminateVariantsVisitor extends Visitor {

        @Override
        public void visit(Rule rule) throws SemanticException {
            rule.acceptChildren(this);
        }

        @Override
        public void visit(Variant variant) throws SemanticException {
            variant.acceptChildren(this);

            TreeNode parent = variant.getParent();
            List<TreeNode> children = variant.getChildren();
            children.forEach(TreeNode::remove);
            if (!variant.returns() && parent != null) {
                TreeNode parentParent = parent.getParent();
                Objects.requireNonNullElse(parentParent, parent).addChildren(children);
            } else if (parent != null) {
                if (parent instanceof Rule) {
                    Subrule artificial = new Subrule(((Rule)parent).getNames().get(0));
                    artificial.addChildren(children);
                    parent.addChild(artificial);
                } else {
                    parent.addChildren(children);
                }
            }
            variant.remove();
        }
    }

    /**
     * Collects all separate paths.
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
