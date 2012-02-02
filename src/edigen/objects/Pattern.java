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
package edigen.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * The pattern decides which variant is selected for a particular rule.
 * @author Matúš Sulír
 */
public class Pattern {

    private BitSequence mask = new BitSequence();
    private BitSequence bits = new BitSequence();
    private List<Rule> rules = new ArrayList<Rule>();
    
    /**
     * Adds the subrule to this pattern.
     * @param rule the subrule
     */
    public void addRule(Rule rule) {
        rules.add(rule);
    }
    
    /**
     * Adds the subrule length to this pattern.
     * 
     * The length is associated with the most recently added subrule.
     * @param length the subrule length
     */
    public void addRuleLength(int length) {
        mask.append(new BitSequence(length));
        bits.append(new BitSequence(length));
    }
    
    /**
     * Adds a constant bit sequence to this pattern.
     * 
     * The sequence will be used during variant matching.
     * @param constant the bit sequence
     */
    public void addConstant(BitSequence constant) {
        mask.append(new BitSequence(constant.getLength(), true));
        bits.append(constant);
    }
}
