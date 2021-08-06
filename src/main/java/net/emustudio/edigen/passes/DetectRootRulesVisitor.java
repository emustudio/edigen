package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;

import java.util.*;

/**
 * Detects root rules, including undefined root rules.
 * If one or more undefined root rules are found, an exception is thrown.
 */
public class DetectRootRulesVisitor extends Visitor {
    private final Set<String> rootRuleNames = new LinkedHashSet<>();
    private final Map<String, Rule> rootRulesByName = new HashMap<>();

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        rootRuleNames.addAll(decoder.getRootRuleNames());
        decoder.acceptChildren(this);
        if (!rootRuleNames.isEmpty()) {
            throw new SemanticException("Root rules were not defined: " + rootRuleNames, decoder);
        }

        // root rules definition might be in a different order than root rules declaration
        Set<Rule> rootRules = new LinkedHashSet<>();
        for (String name : decoder.getRootRuleNames()) {
            rootRules.add(rootRulesByName.get(name));
        }
        decoder.setRootRules(rootRules);
    }

    @Override
    public void visit(Rule rule) throws SemanticException {
        for (String name: rule.getNames()) {
            if (rootRuleNames.remove(name)) {
                rule.setRoot(true, name);
                rootRulesByName.put(name, rule);
            }
        }
    }
}
