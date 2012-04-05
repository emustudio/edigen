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

import edigen.SemanticException;
import edigen.decoder.TreeNode;
import edigen.decoder.Visitor;

/**
 * Instruction decoder rule node.
 * @author Matúš Sulír
 */
public class Rule extends TreeNode {
    
    private String name;
    
    /**
     * Constructs a rule.
     * @param name the rule name
     */
    public Rule(String name) {
        this.name = name;
    }
    
    /**
     * Returns the name of this rule.
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the rule code which will be used as a name of the constant in the
     * generated code.
     * @return the rule code
     */
    public String getCode() {
        return name.toUpperCase();
    }
    
    /**
     * Accepts the visitor.
     * @param visitor the visitor object
     */
    @Override
    public void accept(Visitor visitor) throws SemanticException {
        visitor.visit(this);
    }
    
    /**
     * Returns a string representation of the object containing a rule name.
     * @return the string
     */
    @Override
    public String toString() {
        return "Rule: " + name;
    }
}
