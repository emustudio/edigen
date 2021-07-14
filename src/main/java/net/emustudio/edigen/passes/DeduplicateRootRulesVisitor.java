package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This visitor deduplicates alternative rule names in root rules declaration.
 *
 * E.g.:
 *
 * <pre>
 *     root src, dst;
 *
 *     src, dst = "x": 101 | "y": 110;
 * </pre>
 *
 * It doesn't make any sense to keep both in the root rules, because
 */
public class DeduplicateRootRulesVisitor extends Visitor {

    private final List<List<String>> allRuleNames = new ArrayList<>();

    /**
     * Removes alternate rule names from the root rule declaration.
     *
     * It keeps only the first alternative as it occurs in order, others are removed.
     * @param decoder decoder
     * @throws SemanticException never happens
     */
    @Override
    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);

        Set<String> deduplicatedRootRuleNames = new LinkedHashSet<>();
        for (String name: decoder.getRootRuleNames()) {
            List<String> toRemove = null;
            for (List<String> ruleNames : allRuleNames) {
                if (ruleNames.contains(name)) {
                    deduplicatedRootRuleNames.add(name);
                    toRemove = ruleNames;
                    break;
                }

            }
            if (toRemove != null) {
                allRuleNames.remove(toRemove);
                String removedAlternatives = toRemove.stream().skip(1).collect(Collectors.joining(", "));
                if (!removedAlternatives.isEmpty()) {
                    System.out.println("Removed alternative rule names: " + removedAlternatives);
                }
            }
        }

        decoder.setRootRuleNames(deduplicatedRootRuleNames);
    }

    @Override
    public void visit(Rule rule) throws SemanticException {
        allRuleNames.add(rule.getNames());
    }
}
