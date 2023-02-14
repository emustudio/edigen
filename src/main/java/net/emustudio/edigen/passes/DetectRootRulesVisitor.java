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
    private final Set<String> visitedRuleNames = new HashSet<>();

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

    /**
     * Assigns rule objects to the mapping of names to objects.
     *
     * Also checks if some declared root rule name points to already declared rule with another name. If this case
     * isn't catched, there will be practically multiple tries of decoding the same thing - if it failed once, it
     * certainly fails second time (we assume reading memory is idempotent).
     * @param rule rule object
     * @throws SemanticException if the rule was already declared as root
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        for (String name: rule.getNames()) {
            if (visitedRuleNames.contains(name)) {
                throw new SemanticException("The rule was declared as root more than once (with another name)", rule);
            }
            if (rootRuleNames.remove(name)) {
                rule.setRoot(true, name);
                rootRulesByName.put(name, rule);
                visitedRuleNames.addAll(rule.getNames());
            }
        }
    }
}
