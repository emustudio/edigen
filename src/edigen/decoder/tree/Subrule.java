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
 * A subrule is a rule name contained in a variant.
 * @author Matúš Sulír
 */
public class Subrule extends TreeNode {
    
    private Rule rule;
    private Integer start = null;
    private Integer length = null;
    
    /**
     * Constructs the subrule with the specified length.
     * @param rule the rule which this subrule refers to
     * @param length the rule length
     */
    public Subrule(Rule rule, int length) {
        this.rule = rule;
        this.length = length;
    }
    
    /**
     * Constructs the subrule with an unspecified length.
     * 
     * Can be located only at the end of a variant.
     * @param rule the rule which this subrule refers to
     */
    public Subrule(Rule rule) {
        this.rule = rule;
    }
    
    /**
     * Sets the starting offset relative to the variant start.
     * @param start the starting offset, in bits
     */
    public void setStart(int start) {
        this.start = start;
    }
    
    /**
     * Returns the subrule name, which is the same as the referred rule's name.
     * @return 
     */
    public String getName() {
        return rule.getName();
    }
    
    /**
     * Returns the starting offset.
     * 
     * The result is null if it is not yet determined.
     * @return the starting offset, in bits
     */
    public Integer getStart() {
        return start;
    }
    
    /**
     * Returns the subrule length.
     * 
     * The result is null for a rule with an unspecified length.
     * @return the length in bits
     */
    public Integer getLength() {
        return length;
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
     * Returns a string representation of the object containing the rule name
     * and optionally start and length.
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Subrule: ");
        
        result.append(getName());
        
        if (start != null)
            result.append(", start: ").append(start);
        
        if (length != null)
            result.append(", length: ").append(length);
        
        return result.toString();
    }
}
