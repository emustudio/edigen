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

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        knownRules.addAll(decoder.getRootRuleNames());
        decoder.acceptChildren(this);
    }

    /**
     * In case of the root rule traverses it and saves references to other rules.
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
        if (rule != null && isUnknown(rule)) {
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
