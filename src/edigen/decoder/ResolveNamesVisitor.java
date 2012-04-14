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
package edigen.decoder;

import edigen.SemanticException;
import edigen.Visitor;
import edigen.tree.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A visitor which creates associactions between objects accoring to their names
 * obtained from the input file.
 * 
 * Because the AST was constructed in one pass, backward references are not yet
 * solved. This visitor resolves them (along with the backward references). 
 * @author Matúš Sulír
 */
public class ResolveNamesVisitor extends Visitor {
    
    private Map<String, Rule> rules = new HashMap<String, Rule>();
    private Subrule returnSubrule;

    /**
     * First saves all rule names and then traverses the whole tree.
     * @param decoder the decoder node
     * @throws SemanticException never
     */
    @Override
    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);
        
        for (TreeNode rule : decoder.getChildren())
            rule.acceptChildren(this);
    }

    /**
     * Fills the map of rule names to rules.
     * @param rule the rule node
     */
    @Override
    public void visit(Rule rule) {
        for (String name : rule.getNames()) {
            rules.put(name, rule);
        }
    }

    /**
     * Associates the subrule with the variant which returns it.
     * @param variant the variant node
     * @throws SemanticException never
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        returnSubrule = variant.getReturnSubrule();
        variant.acceptChildren(this);
        
        if (returnSubrule != null)
            variant.setReturnSubrule(returnSubrule);
    }

    /**
     * Associates the subrule with the rule.
     * @param subrule the subrule node
     */
    @Override
    public void visit(Subrule subrule) {
        subrule.setRule(rules.get(subrule.getName()));
        
        if (returnSubrule != null && subrule.getName().equals(returnSubrule.getName()))
            returnSubrule = subrule;
    }

    @Override
    public void visit(Value value) {
        value.setRule(rules.get(value.getName()));
    }
    
}
