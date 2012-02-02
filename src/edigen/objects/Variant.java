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

/**
 * The rule variant.
 * @author Matúš Sulír
 */
public class Variant {
    private enum ReturnType {
        NOTHING,
        STRING,
        SUBRULE
    }
    
    private ReturnType returnType = ReturnType.NOTHING;
    private String returnString;
    private Rule returnSubRule;
    private Pattern pattern = new Pattern();
    
    /**
     * Tells the variant to return the string on match.
     * @param returnString the string to return
     */
    public void setReturnValue(String returnString) {
        returnType = ReturnType.STRING;
        this.returnString = returnString;
    }
    
    /**
     * Tells the variant to return the value of the specified subrule.
     * @param returnSubRule the subrule, must be contained in the pattern
     */
    public void setReturnValue(Rule returnSubRule) {
        returnType = ReturnType.SUBRULE;
        this.returnSubRule = returnSubRule;
    }
    
    /**
     * Returns the pattern object associated with this variant.
     * 
     * Each variant object has exactly one, permanently associated pattern.
     * @return 
     */
    public Pattern getPattern() {
        return pattern;
    }
    
}
