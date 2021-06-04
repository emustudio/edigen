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
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.Disassembler;
import net.emustudio.edigen.nodes.Format;
import net.emustudio.edigen.nodes.TreeNode;

import java.io.Writer;
import java.util.Iterator;

/**
 * A visitor which generates the code of the array of disassembler formats.
 */
public class GenerateFormatsVisitor extends Visitor {
    
    private final PrettyPrinter printer;
    private String formatString;

    /**
     * Constucts the visitor.
     * @param writer the output stream to write the code to
     */
    public GenerateFormatsVisitor(Writer writer) {
        this.printer = new PrettyPrinter(writer);
    }

    /**
     * Writes the formats separated by commas.
     * @param disassembler the disassembler node
     * @throws SemanticException never
     */
    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        Iterator<TreeNode> formats = disassembler.getChildren().iterator();
        
        while (formats.hasNext()) {
            formats.next().accept(this);
            
            String separator = formats.hasNext() ? ", " : "";
            printer.writeLine(formatString + separator);
        }
    }

    /**
     * Saves the format string in the quotes into the variable.
     * @param format the format node 
     */
    @Override
    public void visit(Format format) {
        formatString = '"' + format.getFormatString() + '"';
    }
    
}
