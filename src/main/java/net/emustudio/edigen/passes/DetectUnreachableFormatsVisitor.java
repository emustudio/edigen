package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects unreachable disassembler formats
 */
public class DetectUnreachableFormatsVisitor extends Visitor {

    public static class Unwinded {
        private final Unwinded parent;

        private final List<Unwinded> children = new ArrayList<>();
        private final Set<String> rules = new HashSet<>();

        public Unwinded(Unwinded parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "A{rules=" + rules + "}";
        }

        public String fullToString() {
            return this + childrenToString(2);
        }

        private String childrenToString(int indent) {
            StringBuilder str = new StringBuilder();
            for (Unwinded unwinded : children) {
                str.append("\n");
                if (indent > 0) {
                    str.append(String.format("%" + indent + "s", " "));
                }
                str.append(unwinded.toString());
                str.append(unwinded.childrenToString(indent + 2));
            }
            return str.toString();
        }

        public void removeEmpty() {
            for (Unwinded unwinded : new ArrayList<>(children)) {
                unwinded.removeEmpty();
            }
            if (children.isEmpty() && rules.isEmpty() && parent != null) {
                parent.children.remove(this);
            }
        }

        public Set<Set<String>> collectRuleKeys() {
            Set<Set<String>> result = new HashSet<>();
            if (children.isEmpty()) {
                result.add(new HashSet<>(rules));
            }

            for (Unwinded child : children) {
                for (Set<String> childCollected : child.collectRuleKeys()) {
                    Set<String> current = new HashSet<>(rules);
                    current.addAll(childCollected);
                    result.add(current);
                }
            }
            return result;
        }
    }

    public final List<Unwinded> unwindedRoots = new ArrayList<>();
    private Unwinded currentUnwinded;
    private String currentRuleFieldName;

    private final Set<Set<String>> allowedFormats = new HashSet<>();
    private final Set<Set<String>> usedFormats = new HashSet<>();
    private Set<String> currentFormat = new HashSet<>();

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        for (Rule rule : decoder.getRootRules()) {
            Unwinded unwinded = new Unwinded(null);
            unwindedRoots.add(unwinded);
            currentUnwinded = unwinded;
            currentRuleFieldName = rule.getFieldName();
            rule.accept(this);
            unwinded.removeEmpty();
            allowedFormats.addAll(unwinded.collectRuleKeys());
        }
    }

    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        disassembler.acceptChildren(this);

        usedFormats.removeAll(allowedFormats);
        if (!usedFormats.isEmpty()) {
            throw new SemanticException("Some formats are unreachable: " + usedFormats, disassembler);
        }
    }

    @Override
    public void visit(Variant variant) throws SemanticException {
        if (variant.returns()) {
            currentUnwinded.rules.add(currentRuleFieldName);
        }

        Unwinded unwinded = new Unwinded(currentUnwinded);
        currentUnwinded.children.add(unwinded);
        Unwinded oldCurrentUnwinded = currentUnwinded;
        String oldCurrentRuleFieldName = currentRuleFieldName;
        currentUnwinded = unwinded;

        variant.acceptChildren(this);
        currentUnwinded = oldCurrentUnwinded;
        currentRuleFieldName = oldCurrentRuleFieldName;
    }

    @Override
    public void visit(Subrule subrule) throws SemanticException {
        Rule rule = subrule.getRule();
        if (rule != null) {
            currentRuleFieldName = subrule.getFieldName();
            rule.accept(this);
        }
    }

    @Override
    public void visit(Format format) throws SemanticException {
        currentFormat = new HashSet<>();
        format.acceptChildren(this);
        usedFormats.add(currentFormat);
    }

    @Override
    public void visit(Value value) throws SemanticException {
        currentFormat.add(value.getName());
    }
}
