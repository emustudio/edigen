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
package edigen.decoder.tree;

import edigen.decoder.TreeNode;
import edigen.decoder.Visitor;
import java.util.HashMap;
import java.util.Map;

/**
 * The root node of the instruction decoder generator tree.
 * @author Matúš Sulír
 */
public class Decoder extends TreeNode {
    
    private Map<String, Rule> rules = new HashMap<String, Rule>();
    
    /**
     * Adds a new rule to the decoder as a child.
     * @param rule the rule object
     */
    public void addRule(Rule rule) {
        rules.put(rule.getName(), rule);
        addChild(rule);
    }
    
    /**
     * Returns a rule identified by the particular name.
     * @param name the rule name
     * @return the rule object
     */
    public Rule getRuleByName(String name) {
        return rules.get(name);
    }
    
    /**
     * Accepts the visitor.
     * @param visitor the visitor object
     */
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns a string representation of the object.
     * @return the string
     */
    @Override
    public String toString() {
        return "Decoder";
    }
}
