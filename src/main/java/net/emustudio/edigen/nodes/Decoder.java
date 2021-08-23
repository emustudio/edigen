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
 * The root node of the instruction decoder subtree.
 */
public class Decoder extends TreeNode {
    /**
     * Size (in bits) of one unit which decoder can read at once
     */
    public static final int UNIT_SIZE_BITS = 32;

    private final Set<String> declaredRootRuleNames = new LinkedHashSet<>();
    private final Set<Rule> rootRules = new LinkedHashSet<>();

    /**
     * Creates new decoder.
     *
     * @param declaredRootRuleNames explicitly define root rules of the decoder.
     */
    public Decoder(Set<String> declaredRootRuleNames) {
        this.declaredRootRuleNames.addAll(Objects.requireNonNull(declaredRootRuleNames));
    }

    /**
     * Creates new decoder.
     *
     * @param declaredRootRuleNames explicitly define root rules of the decoder.
     */
    public Decoder(String... declaredRootRuleNames) {
        this.declaredRootRuleNames.addAll(Arrays.asList(declaredRootRuleNames));
    }

    /**
     * Creates new decoder.
     * Root rule will be the first child
     */
    public Decoder() {
    }

    /**
     * Returns the starting rules names.
     * 
     * @return the root rules names
     */
    public Set<String> getRootRuleNames() {
        if (declaredRootRuleNames.isEmpty()) {
            return Collections.singleton(((Rule)getChild(0)).getNames().get(0));
        }
        return Collections.unmodifiableSet(declaredRootRuleNames);
    }

    /**
     * Returns all starting rule variants.
     * Note root rules must be explicitly set; having only declared root rule names is not enough.
     * @return root rules
     */
    public Set<Rule> getRootRules() {
        return Collections.unmodifiableSet(rootRules);
    }

    /**
     * Returns the first starting rule.
     * @return the first root rule object
     */
    public Rule getRootRule() {
        return rootRules.iterator().next();
    }

    /**
     * Set starting rules.
     * Size of declared root rules and root rules objects must be the same.
     * @param rootRules root rule objects
     */
    public void setRootRules(Set<Rule> rootRules) {
        if (declaredRootRuleNames.size() != rootRules.size()) {
            throw new IllegalArgumentException("Root rule sizes do not match");
        }

        Iterator<Rule> ruleIterator = rootRules.iterator();
        for (String name : declaredRootRuleNames) {
            Rule rule = ruleIterator.next();
            if (!rule.getNames().contains(name)) {
                throw new IllegalArgumentException("Declared root rule name '" + name +
                        "' does not match with provided root rule");
            }
        }
        this.rootRules.clear();
        this.rootRules.addAll(rootRules);
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
     * Returns a string representation of the object.
     * @return the string
     */
    @Override
    public String toString() {
        return "Decoder";
    }
}
