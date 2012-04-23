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
package edigen.disasm;

import edigen.SemanticException;
import edigen.Visitor;
import edigen.tree.Format;
import edigen.tree.Value;
import edigen.tree.Variant;
import java.util.HashSet;
import java.util.Set;

/**
 * A visitor which checks for semantic errors in the disassembler specification.
 * @author Matúš Sulír
 */
public class SemanticCheckVisitor extends Visitor {
    
    private Set<Set<String>> formatSet = new HashSet<Set<String>>();
    private Set<String> valueSet = new HashSet<String>();
    private boolean ruleReturns;

    /**
     * Finds out whether the particular set of rules was not already used.
     * 
     * Otherwise it would be ambiguous which format to apply when the decoded
     * instruction contained this set of rules.
     * @param format the format node
     * @throws SemanticException if the set of valus was used in multiple
     *         formats
     */
    @Override
    public void visit(Format format) throws SemanticException {
        valueSet.clear();
        format.acceptChildren(this);
        
        if (!formatSet.contains(valueSet)) {
            formatSet.add(valueSet);
        } else {
            StringBuilder values = new StringBuilder();
            
            for (String value : valueSet) {
                if (values.length() != 0)
                    values.append(", ");
                
                values.append(value);
            }
            
            throw new SemanticException("Set of values \"" + values.toString()
                    + "\" is contained in multiple disassembler formats");
        }
    }

    /**
     * Finds out whether at least one rule's variant can return a value.
     * 
     * Probably only the fields for variants returning a value will be present
     * in the generated code, so using other variants in a disassembler would
     * cause a syntax error.
     * @param value the value node
     * @throws SemanticException if the rule which this value refers to
     *         never returns a value
     */
    @Override
    public void visit(Value value) throws SemanticException {
        ruleReturns = false;
        value.getRule().accept(this);
        
        if (ruleReturns) {
            valueSet.add(value.getName());
        } else {
            throw new SemanticException("Rule \"" + value.getName() + "\" never"
                    + " returns a value, but is used in the disassembler");
        }
    }

    /**
     * Sets the flag if the variant returns something.
     * @param variant the variant node
     */
    @Override
    public void visit(Variant variant) {
        if (variant.returns())
            ruleReturns = true;
    }
    
}
