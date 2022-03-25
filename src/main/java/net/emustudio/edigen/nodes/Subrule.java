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
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;

import java.util.Objects;

/**
 * A subrule is a rule name contained in a variant.
 */
public class Subrule extends TreeNode {
    
    private final String name;
    private Integer start;
    private final Integer length;
    private final Pattern prePattern;
    private Rule rule;
    
    /**
     * Constructs the subrule with an unspecified length.
     * 
     * Can be located only at the end of a variant.
     * @param name the subrule name
     */
    public Subrule(String name) {
        this(name, null);
    }
    
    /**
     * Constructs the subrule with the specified pre-pattern.
     * 
     * Can be located only at the end of a variant.
     * @param name the subrule name
     * @param prePattern the forward pattern information
     */
    public Subrule(String name, Pattern prePattern) {
        this(name, null, prePattern);
    }
    
    /**
     * Constructs the subrule with the specified length and pre-pattern.
     * @param name the subrule name
     * @param length the subrule length
     * @param prePattern the forward pattern information
     */
    public Subrule(String name, Integer length, Pattern prePattern) {
        this.name = name;
        this.length = length;
        this.prePattern = prePattern;
    }
    
    /**
     * Returns the subrule name, as obtained from the input.
     * @return the subrule name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the starting offset relative to the variant start.
     * 
     * The result is null if it is not yet determined.
     * @return the starting offset, in bits
     */
    public Integer getStart() {
        return start;
    }
    
    /**
     * Sets the starting offset relative to the variant start.
     * @param start the starting offset, in bits
     */
    public void setStart(int start) {
        this.start = start;
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
     * Returns the rule to which this subrule refers.
     * 
     * The result is null if the name was not yet resolved or the subrule
     * does not refer to any rule.
     * @return the rule object or null
     */
    public Rule getRule() {
        return rule;
    }
    
    /**
     * Returns the forward pattern information.
     * @return the pre-pattern
     */
    public Pattern getPrePattern() {
        return prePattern;
    }

    /**
     * Specifies to which rule this subrule refers.
     * 
     * Used during name resolution.
     * @param rule the rule object
     * @return this
     */
    public Subrule setRule(Rule rule) {
        this.rule = rule;
        return this;
    }
    
    /**
     * Returns the field name which should be generated for this subrule.
     * @return the field name
     */
    public String getFieldName() {
        return rule.getFieldName(name);
    }
    
    /**
     * Accepts the visitor.
     * @param visitor the visitor object
     * @throws SemanticException depends on the specific visitor
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

        if (prePattern != null)
            result.append(", prePattern: ").append(prePattern.getBits());

        if (rule != null)
            result.append(", rule: ").append(rule.getLabel());
        
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subrule subrule = (Subrule) o;

        if (!Objects.equals(name, subrule.name)) return false;
        if (!Objects.equals(start, subrule.start)) return false;
        if (!Objects.equals(length, subrule.length)) return false;
        if (!Objects.equals(prePattern, subrule.prePattern)) return false;
        return Objects.equals(rule, subrule.rule);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + (prePattern != null ? prePattern.hashCode() : 0);
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        return result;
    }
}
