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
    private final Set<String> rootRuleNames;

    /**
     * Creates new decoder.
     *
     * @param rootRuleNames explicitly define root rules of the decoder.
     */
    public Decoder(Set<String> rootRuleNames) {
        this.rootRuleNames = Objects.requireNonNull(rootRuleNames);
    }

    /**
     * Creates new decoder.
     *
     * @param rootRuleNames explicitly define root rules of the decoder.
     */
    public Decoder(String... rootRuleNames) {
        this.rootRuleNames = new LinkedHashSet<>(Arrays.asList(rootRuleNames));
    }

    /**
     * Creates new decoder.
     * Root rule will be the first child
     */
    public Decoder() {
        rootRuleNames = Collections.emptySet();
    }

    /**
     * Returns the starting rules names.
     * 
     * @return the root rules names
     */
    public Set<String> getRootRuleNames() {
        if (rootRuleNames.isEmpty()) {
            return Collections.singleton(((Rule)getChild(0)).getNames().get(0));
        }
        return rootRuleNames;
    }

    /**
     * Set starting rule names.
     *
     * @param rootRuleNames rule names
     */
    public void setRootRuleNames(Set<String> rootRuleNames) {
        this.rootRuleNames.clear();
        this.rootRuleNames.addAll(rootRuleNames);
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
