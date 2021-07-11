package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;

import java.util.HashSet;
import java.util.Set;

public class DetectNonExistingRootRulesVisitor extends Visitor {
    private final Set<String> rootRuleNames = new HashSet<>();

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        rootRuleNames.addAll(decoder.getRootRuleNames());
        decoder.acceptChildren(this);
        if (!rootRuleNames.isEmpty()) {
            throw new SemanticException("Root rules were not defined: " + rootRuleNames, decoder);
        }
    }

    @Override
    public void visit(Rule rule) throws SemanticException {
        for (String name: rule.getNames()) {
            rootRuleNames.remove(name);
        }
    }
}
