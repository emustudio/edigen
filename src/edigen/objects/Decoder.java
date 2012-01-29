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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * The "root" class of the instruction decoder generator.
 * 
 * Contains references to all rules.
 * @author Matúš Sulír
 */
public class Decoder {
    private Map<String, Rule> rulesByName = new HashMap<String, Rule>();
    private Set<Rule> uniqueRules = new HashSet<Rule>();
    
    /**
     * Adds a rule to the decoder.
     * 
     * One rule can have multiple names. This method tracks both a unique rule
     * set and associations from names to rules.
     * @param name the rule name
     * @param rule the rule object
     */
    public void addRule(Rule rule, String name) {
        rulesByName.put(name, rule);
        uniqueRules.add(rule);
    }
    
    /**
     * Returns a rule identified by the particular name.
     * @param name the rule name
     * @return the rule object
     */
    public Rule getRuleByName(String name) {
        return rulesByName.get(name);
    }
    
    /**
     * Returns a set of all unique rules.
     * @return the rule set
     */
    public Set<Rule> getUniqueRules() {
        return uniqueRules;
    }
}
