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
package edigen;

import edigen.objects.Decoder;
import edigen.objects.Rule;
import edigen.tree.RuleNameSet;
import edigen.tree.SimpleNode;

/**
 * The AST-pass which registers rule names and checks for duplicate rule names.
 * @author Matúš Sulír
 */
public class NamePass {
    
    private Decoder decoder;
    
    /**
     * Constructs a name-pass.
     * @param decoder the decoder generator where rules will be added
     */
    public NamePass(Decoder decoder) {
        this.decoder = decoder;
    }
    
    /**
     * Recursively traverses the tree nodes.
     * 
     * Checks whether there are no duplicate rule names. For each RuleNameSet,
     * adds an empty rule associated with all corresponding names to
     * the decoder.
     * @param node the node to start checking
     * @throws SemanticException when a duplicate rule name is detected
     */
    public void checkNode(SimpleNode node) throws SemanticException {
        int childrenCount = node.jjtGetNumChildren();
        
        if (node instanceof RuleNameSet) {
            Rule emptyRule = new Rule();
            
            for (int i = 0; i < childrenCount; i++) {
                SimpleNode child = (SimpleNode) node.jjtGetChild(i);
                addRule((String) child.jjtGetValue(), emptyRule);
            }
        } else {
            for (int i = 0; i < childrenCount; i++)
                checkNode((SimpleNode) node.jjtGetChild(i));
        }
    }
    
    /**
     * Registers the decoder generator rule if the name is not already
     * registered.
     * @param name the rule name
     * @param rule the rule object
     * @throws SemanticException when a duplicate rule name is detected
     */
    private void addRule(String name, Rule rule) throws SemanticException {
        if (decoder.getRuleByName(name) == null) {
            decoder.addRule(rule, name);
        } else {
            throw new SemanticException("Rule \"" + name + "\" is declared more than once.");
        }
    }
}
