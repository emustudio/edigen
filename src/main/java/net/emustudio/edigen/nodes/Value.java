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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The disassembler value node - bound to an instruction decoder rule name.
 */
public class Value extends TreeNode {
    
    private final String name;
    private Rule rule;
    private List<String> strategies = Collections.singletonList("little_endian");

    /**
     * Constructs the value.
     * @param name the rule name
     */
    public Value(String name) {
        this.name = name;
    }

    /**
     * Returns the name, as obtained from the input.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the rule associated with this value.
     * @return the rule object
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Sets the rule associated with this value.
     * @param rule the rule object
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }
    
    /**
     * Returns the field name which should be generated for this value.
     * @return the field name
     */
    public String getFieldName() {
        return rule.getFieldName(name);
    }

    /**
     * Returns the constant decoding strategies.
     * @return list of strategy names - e.g., "little_endian"
     */
    public List<String> getStrategies() {
        return strategies;
    }

    /**
     * Sets the constant decoding strategy.
     * @param strategies the strategy name - e.g., "big_endian"
     */
    public void setStrategies(List<String> strategies) {
        this.strategies = strategies;
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
        return "Value: " + name;
    }
}
