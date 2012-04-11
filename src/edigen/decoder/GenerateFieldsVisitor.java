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
package edigen.decoder;

import edigen.Visitor;
import edigen.tree.Rule;
import edigen.util.PrettyPrinter;
import java.io.Writer;

/**
 * A visitor which generates Java source code of the instruction decoder fields
 * for all rules.
 * 
 * Each rule is given a unique integral constant which can be later used in
 * a disassembler or an emulator.
 * @author Matúš Sulír
 */
public class GenerateFieldsVisitor extends Visitor {

    private PrettyPrinter printer;
    int ruleNumber = 0;

    /**
     * Constucts the visitor.
     * @param writer the output stream to write the code to
     */
    public GenerateFieldsVisitor(Writer writer) {
        this.printer = new PrettyPrinter(writer);
    }

    /**
     * Writes the constants for the particular rule.
     * @param rule the rule node
     */
    @Override
    public void visit(Rule rule) {
        for (String name : rule.getNames()) {
            printer.writeLine("private static final int "
                    + rule.getFieldName(name) + " = " + ruleNumber++  + ";");
        }
    }
    
}
