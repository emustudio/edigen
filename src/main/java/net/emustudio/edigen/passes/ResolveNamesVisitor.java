/*
 * Copyright (C) 2012 Matúš Sulír
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.*;

/**
 * A visitor which creates associations between objects according to their names
 * obtained from the input file.
 *
 * Missing implicit subrules are inferred.
 * 
 * Because the AST was constructed in one pass, backward references are not yet
 * solved. This visitor resolves them (along with the backward references).
 */
public class ResolveNamesVisitor extends Visitor {
    
    private final Map<String, Rule> rules = new LinkedHashMap<>();
    private final List<Rule> inferredRules = new ArrayList<>();
    private final Set<String> ruleFieldNames = new LinkedHashSet<>();
    private String searchedSubrule;
    private Subrule foundSubrule;

    /**
     * First saves all rule names and then traverses the rule subtrees.
     * @param decoder the decoder node
     * @throws SemanticException never
     */
    @Override
    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);
        
        for (TreeNode rule : decoder.getChildren())
            rule.acceptChildren(this);

        decoder.addChildren(inferredRules.toArray(new Rule[0]));
    }

    /**
     * Adds item(s) to the map from rule names to rules.
     * @param rule the rule node
     * @throws SemanticException if the rule was already defined
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        for (String name : rule.getNames()) {
            if (!rules.containsKey(name)) {
                rules.put(name, rule);
                String field = rule.getFieldName(name);
                
                if (!ruleFieldNames.contains(field)) {
                    ruleFieldNames.add(field);
                } else {
                    throw new SemanticException("Rule field \"" + field
                            + "\" is generated multiple times", rule);
                }
            } else {
                throw new SemanticException("Rule \"" + name
                        + "\" is defined multiple times", rule);
            }
        }
    }

    /**
     * Associates the variant with the subrule which it returns.
     * @param variant the variant node
     * @throws SemanticException if the variant returns nonexistent subrule
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        if (variant.getReturnSubrule() == null)
            searchedSubrule = null;
        else
            searchedSubrule = variant.getReturnSubrule().getName();
        
        foundSubrule = null;
        variant.acceptChildren(this);
        
        if (searchedSubrule != null) {
            if (foundSubrule != null)
                variant.setReturnSubrule(foundSubrule);
            else
                throw new SemanticException("Variant returns nonexistent"
                        + " subrule \"" + searchedSubrule + '"', variant);
        }
    }

    /**
     * Associates the subrule with the rule.
     * @param subrule the subrule node
     * @throws SemanticException on subrule-related semantic errors
     */
    @Override
    public void visit(Subrule subrule) throws SemanticException {
        if (subrule.getLength() != null && subrule.getName().equals(searchedSubrule)) {
            if (foundSubrule == null)
                foundSubrule = subrule;
            else
                throw new SemanticException("Subrule \"" + searchedSubrule
                        + "\" is present multiple times in a variant which"
                        + " returns it", subrule);
        } else {
            Rule rule = rules.get(subrule.getName());
            
            if (rule != null)
                subrule.setRule(rule);
            else if (subrule.getLength() != null) {
                Rule inferred = inferImplicitRule(subrule);
                rules.put(subrule.getName(), inferred);
                inferredRules.add(inferred);
                subrule.setRule(inferred);
            } else {
                throw new SemanticException("Subrule \"" + subrule.getName()
                        + "\" refers to a nonexistent rule", subrule);
            }
        }
    }

    /**
     * Associates the value with the rule.
     * @param value the value node
     * @throws SemanticException if the value refers to a nonexistent rule
     */
    @Override
    public void visit(Value value) throws SemanticException {
        String name = value.getName();
        Rule rule = rules.get(name);
        
        if (rule != null) {
            value.setRule(rule);
        } else {
            throw new SemanticException("Disassembler value \""
                    + name + "\" refers to a nonexistent rule", value);
        }
    }

    private Rule inferImplicitRule(Subrule originalSubrule) {
        Rule rule = new Rule(originalSubrule.getName());
        Variant variant = new Variant();
        Subrule subrule = new Subrule("arg", originalSubrule.getLength(), originalSubrule.getPrePattern());
        variant.addChild(subrule);
        variant.setReturnSubrule(subrule);
        rule.addChild(variant);
        return rule;
    }
}
