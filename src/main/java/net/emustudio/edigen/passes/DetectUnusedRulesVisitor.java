package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.Subrule;

import java.util.HashSet;
import java.util.Set;

/**
 * A visitor which finds unused rules.
 * Unused rules are treated as errors.
 */
public class DetectUnusedRulesVisitor extends Visitor {

    private static final String MESSAGE = "Unused rule detected: \"%s\"";
    private boolean rootRuleVisited;
    private final Set<String> knownRules = new HashSet<>();

    /**
     * In case the root rule traverses it and saves references to other rules.
     * Otherwise detects possible unused rule by checking its name(s) in the saved references.
     *
     * @param rule the rule node
     * @throws SemanticException when unused rule is detected
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        if (!rootRuleVisited) {
            rootRuleVisited = true;
            knownRules.addAll(rule.getNames());
            rule.acceptChildren(this);
        }
        if (isUnknown(rule)) {
            throw new SemanticException(String.format(MESSAGE, rule.getLabel()), rule);
        }
    }

    @Override
    public void visit(Subrule subrule) throws SemanticException {
        Rule rule = subrule.getRule();
        if (isUnknown(rule)) {
            knownRules.addAll(rule.getNames());
            rule.acceptChildren(this);
        }
    }

    private boolean isUnknown(Rule rule) {
        for (String name : rule.getNames()) {
            if (knownRules.contains(name)) {
                return false;
            }
        }
        return true;
    }
}
