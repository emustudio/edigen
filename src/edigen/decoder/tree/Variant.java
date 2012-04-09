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
 * Rule variant node.
 * 
 * One of the instruction decoder's task is to find out which variant of the
 * particular rule matches against the part of the decoded instruction.
 * @author Matúš Sulír
 */
public class Variant extends TreeNode {
    
    private enum ReturnType {
        NOTHING,
        STRING,
        SUBRULE
    }
    
    private ReturnType returnType = ReturnType.NOTHING;
    private String returnString;
    private Subrule returnSubrule;
    
    /**
     * Returns the string which this variant returns.
     * @return the string, or null if the variant returns a subrule or nothing
     */
    public String getReturnString() {
        if (returnType == ReturnType.STRING)
            return returnString;
        else
            return null;
    }
    
    /**
     * Tells the variant to return the string on match.
     * @param returnString the string to return
     */
    public void setReturnString(String returnString) {
        returnType = ReturnType.STRING;
        this.returnString = returnString;
    }
    
    /**
     * Returns the subrule which this variant returns.
     * @return the subrule, or null if the variant returns a string or nothing
     */
    public Subrule getReturnSubrule() {
        if (returnType == ReturnType.SUBRULE)
            return returnSubrule;
        else
            return null;
    }
    
    /**
     * Tells the variant to return the value of the specified subrule.
     * @param returnRule the subrule, must be contained in the pattern
     */
    public void setReturnSubrule(Subrule returnRule) {
        returnType = ReturnType.SUBRULE;
        this.returnSubrule = returnRule;
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
     * Returns the mask as a string in binary notation.
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Variant");
        
        if (returnType == ReturnType.STRING)
            result.append(": return \"").append(returnString).append("\"");
        else if (returnType == ReturnType.SUBRULE)
            result.append(": return ").append(returnSubrule);
        
        return result.toString();
    }
}
