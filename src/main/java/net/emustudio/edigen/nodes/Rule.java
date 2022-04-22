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

import java.util.*;

/**
 * Instruction decoder rule node.
 */
public class Rule extends TreeNode {
    
    private final List<String> names;
    private boolean isRoot;
    private String rootRuleName;

    /**
     * Constructs a rule with one or more names.
     * @param names the list of all names of this rule
     */
    public Rule(List<String> names) {
        this.names = names;
    }

    /**
     * Constructs a rule with one name.
     * @param name the name of this rule
     */
    public Rule(String name) {
        this.names = Collections.singletonList(name);
    }

    /**
     * Returns a list of all names of this rule.
     * @return the list of names
     */
    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }
    
    /**
     * Returns true if this rule has only one name (not a list of names
     * separated by commas).
     * @return true if the rule has only one name, false otherwise
     */
    public boolean hasOnlyOneName() {
        return names.size() == 1;
    }
    
    /**
     * Returns a name of the method which should be generated for this rule.
     * @return the method name
     */
    public String getMethodName() {
        return names.get(0);
    }
    
    /**
     * Returns a field name which should be generated for this rule (key).
     * @param ruleName the particular rule name (one rule can have multiple
     *        names - keys)
     * @return the name of a constant for the given key
     */
    public String getFieldName(String ruleName) {
        return ruleName.toUpperCase();
    }

    /**
     * Returns a field name of this rule (key). If the rule has more names, return the key of the root rule name.
     * @return the name of a constant for the given key
     */
    public String getFieldName() {
        return getFieldName(getRootRuleName());
    }

    /**
     * Returns a human-readable label of this rule - a name or a list of names
     * separated by commas.
     * @return the label
     */
    public String getLabel() {
        Iterator<String> nameIterator = names.iterator();
        StringBuilder result = new StringBuilder();
        
        while (nameIterator.hasNext()) {
            result.append(nameIterator.next());
            
            if (nameIterator.hasNext())
                result.append(", ");
        }
        
        return result.toString();
    }

    /**
     * Determines if it is a root rule
     * @return true if it is a root rule, false otherwise
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Get root rule name (if this rule is root).
     * @return root rule name if this rule is root; null otherwise
     */
    public String getRootRuleName() {
        return rootRuleName;
    }

    /**
     * Sets if this rule is a root rule.
     * @param isRoot true if it is a root rule, false otherwise
     * @param rootRuleName root rule name used in root rules declaration
     * @return this
     */
    public Rule setRoot(boolean isRoot, String rootRuleName) {
        if (!names.contains(rootRuleName)) {
            throw new IllegalArgumentException("Root rule name must be one of rule names!");
        }
        this.isRoot = isRoot;
        this.rootRuleName = rootRuleName;
        return this;
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
     * Returns a string representation of the object containing a rule name.
     * @return the string
     */
    @Override
    public String toString() {
        return "Rule: " + getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        return Objects.equals(names, rule.names);
    }

    @Override
    public int hashCode() {
        return names != null ? names.hashCode() : 0;
    }

    @Override
    public TreeNode shallowCopy() {
        Rule cp = new Rule(names);
        cp.setRoot(isRoot, rootRuleName);
        return cp;
    }
}
