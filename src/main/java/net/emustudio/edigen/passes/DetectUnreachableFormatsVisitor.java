/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.*;

/**
 * Detects unreachable disassembler formats.
 * <pre>
 * {@code
 *   root instruction;
 *   instruction = "nop": 00000000 | other ;
 *
 *   other = "hey": 10000000 | "arg %d": 10000001 arg ;
 *   arg = arg: arg(8);
 *
 *   %%
 *
 *   "%s" = instruction arg;    // unreachable
 * }
 * </pre>
 * <p>
 * Current algorithm:
 * <ul>
 *     <li>Starting from the declared root rules, build the graph of reachable rule invocations. An invocation is
 *     identified by the target rule object and the decoder key name used to reach it, because one rule can participate
 *     in decoding under multiple names.</li>
 *     <li>For each reachable invocation, compute the set of decoder key combinations that can be produced by that
 *     invocation. The computation is repeated until no invocation gains any new key combination.</li>
 *     <li>A returning variant contributes its own decoder key name to every combination it produces.</li>
 *     <li>A non-returning variant contributes only the decoder keys produced by the subrules it reaches.</li>
 *     <li>When one variant contains multiple subrules, their reachable key combinations are combined so that the
 *     resulting format key set contains all decoder keys required by that variant.</li>
 *     <li>Subrules without an associated rule do not contribute a decoder key and do not extend the reachable key set.</li>
 *     <li>A variant that neither returns a decoder key nor reaches any variant that does return one produces no
 *     reachable format key set.</li>
 *     <li>Disassembler formats are compared as unordered sets of decoder keys, so value order in the format does not
 *     matter.</li>
 * </ul>
 * The visitor therefore reports every disassembler format whose decoder key set cannot be produced by any reachable
 * decoder path, while allowing recursive rule references to participate in the analysis.
 */
public class DetectUnreachableFormatsVisitor extends Visitor {

    /**
     * Constructs a new detect-unreachable-formats visitor.
     */
    public DetectUnreachableFormatsVisitor() {
    }

    private final Set<Set<String>> reachable = new HashSet<>();
    private final Set<Set<String>> formats = new HashSet<>();
    private Set<String> currentFormat;

    /**
     * Returns the set of reachable format sets.
     *
     * @return the reachable format sets
     */
    public Set<Set<String>> getReachable() {
        return new HashSet<>(reachable);
    }

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        Map<Rule, List<Variant>> reachableRuleVariants = new IdentityHashMap<>();
        for (Rule rootRule : decoder.getRootRules()) {
            collectReachableRuleVariants(rootRule, reachableRuleVariants);
        }

        Set<RuleInvocation> reachableInvocations = new LinkedHashSet<>();
        for (Rule rootRule : decoder.getRootRules()) {
            reachableInvocations.add(new RuleInvocation(rootRule, rootInvocationName(rootRule)));
        }
        for (List<Variant> variants : reachableRuleVariants.values()) {
            for (Variant variant : variants) {
                reachableInvocations.addAll(collectReferencedInvocations(variant));
            }
        }

        Map<RuleInvocation, Set<Set<String>>> reachableFormatKeysByInvocation = new LinkedHashMap<>();
        for (RuleInvocation invocation : reachableInvocations) {
            reachableFormatKeysByInvocation.put(invocation, Set.of());
        }

        boolean changed;
        do {
            changed = false;
            Map<RuleInvocation, Set<Set<String>>> nextReachableFormatKeysByInvocation = new LinkedHashMap<>();

            for (RuleInvocation invocation : reachableInvocations) {
                Set<Set<String>> nextReachableFormatKeys = computeReachableFormatKeysForInvocation(
                        invocation,
                        reachableRuleVariants,
                        reachableFormatKeysByInvocation
                );
                nextReachableFormatKeysByInvocation.put(invocation, nextReachableFormatKeys);
                if (!nextReachableFormatKeys.equals(reachableFormatKeysByInvocation.get(invocation))) {
                    changed = true;
                }
            }

            reachableFormatKeysByInvocation = nextReachableFormatKeysByInvocation;
        } while (changed);

        for (Rule rootRule : decoder.getRootRules()) {
            reachable.addAll(reachableFormatKeysByInvocation.getOrDefault(
                    new RuleInvocation(rootRule, rootInvocationName(rootRule)),
                    Set.of()
            ));
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

    private void collectReachableRuleVariants(Rule rule, Map<Rule, List<Variant>> reachableRuleVariants) {
        if (reachableRuleVariants.containsKey(rule)) {
            return;
        }

        List<Variant> reachableVariants = collectReachableVariants(rule);
        reachableRuleVariants.put(rule, reachableVariants);

        for (Variant variant : reachableVariants) {
            for (RuleInvocation invocation : collectReferencedInvocations(variant)) {
                collectReachableRuleVariants(invocation.rule, reachableRuleVariants);
            }
        }
    }

    private List<Variant> collectReachableVariants(TreeNode node) {
        List<Variant> reachableVariants = new ArrayList<>();

        for (TreeNode child : node.getChildren()) {
            if (child instanceof Variant) {
                reachableVariants.add((Variant) child);
            } else {
                reachableVariants.addAll(collectReachableVariants(child));
            }
        }

        return reachableVariants;
    }

    private Set<RuleInvocation> collectReferencedInvocations(Variant variant) {
        Set<RuleInvocation> referencedInvocations = new LinkedHashSet<>();

        for (TreeNode child : variant.getChildren()) {
            if (child instanceof Subrule) {
                Subrule subrule = (Subrule) child;
                if (subrule.getRule() != null) {
                    referencedInvocations.add(new RuleInvocation(subrule.getRule(), subrule.getName()));
                }
            }
        }

        return referencedInvocations;
    }

    private Set<Set<String>> computeReachableFormatKeysForInvocation(
            RuleInvocation invocation,
            Map<Rule, List<Variant>> reachableRuleVariants,
            Map<RuleInvocation, Set<Set<String>>> knownReachableFormatKeysByInvocation
    ) {
        Set<Set<String>> reachableFormatKeys = new HashSet<>();

        for (Variant variant : reachableRuleVariants.getOrDefault(invocation.rule, List.of())) {
            reachableFormatKeys.addAll(
                    computeReachableFormatKeysForVariant(
                            invocation,
                            variant,
                            knownReachableFormatKeysByInvocation
                    )
            );
        }

        return reachableFormatKeys;
    }

    private Set<Set<String>> computeReachableFormatKeysForVariant(
            RuleInvocation invocation,
            Variant variant,
            Map<RuleInvocation, Set<Set<String>>> knownReachableFormatKeysByInvocation
    ) {
        List<RuleInvocation> childInvocations = new ArrayList<>();

        for (TreeNode child : variant.getChildren()) {
            if (child instanceof Subrule) {
                Subrule subrule = (Subrule) child;
                if (subrule.getRule() != null) {
                    childInvocations.add(new RuleInvocation(subrule.getRule(), subrule.getName()));
                }
            }
        }

        if (childInvocations.isEmpty()) {
            if (!variant.returns()) {
                return Set.of();
            }
            return Set.of(new HashSet<>(Set.of(invocation.name)));
        }

        Set<Set<String>> combinedReachableFormatKeys = new HashSet<>();
        if (variant.returns()) {
            combinedReachableFormatKeys.add(new HashSet<>(Set.of(invocation.name)));
        } else {
            combinedReachableFormatKeys.add(new HashSet<>());
        }

        for (RuleInvocation childInvocation : childInvocations) {
            Set<Set<String>> childReachableFormatKeys = knownReachableFormatKeysByInvocation.getOrDefault(
                    childInvocation,
                    Set.of()
            );
            if (childReachableFormatKeys.isEmpty()) {
                return Set.of();
            }

            Set<Set<String>> nextCombinedReachableFormatKeys = new HashSet<>();
            for (Set<String> combinedReachableFormatKey : combinedReachableFormatKeys) {
                for (Set<String> childReachableFormatKey : childReachableFormatKeys) {
                    Set<String> reachableFormatKey = new HashSet<>(combinedReachableFormatKey);
                    reachableFormatKey.addAll(childReachableFormatKey);
                    nextCombinedReachableFormatKeys.add(reachableFormatKey);
                }
            }
            combinedReachableFormatKeys = nextCombinedReachableFormatKeys;
        }

        return combinedReachableFormatKeys;
    }

    private String rootInvocationName(Rule rule) {
        String name = rule.getRootRuleName();
        if (name != null) {
            return name;
        }
        return rule.getNames().get(0);
    }

    private static final class RuleInvocation {
        private final Rule rule;
        private final String name;

        private RuleInvocation(Rule rule, String name) {
            this.rule = rule;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RuleInvocation that = (RuleInvocation) o;
            return rule == that.rule && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(rule) + Objects.hashCode(name);
        }
    }
}
