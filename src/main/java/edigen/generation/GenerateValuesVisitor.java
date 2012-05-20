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
package edigen.generation;

import edigen.SemanticException;
import edigen.Visitor;
import edigen.nodes.Disassembler;
import edigen.nodes.Format;
import edigen.nodes.TreeNode;
import edigen.nodes.Value;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * A visitor which generates the code of the two-dimensional array of
 * disassembler values.
 * @author Matúš Sulír
 */
public class GenerateValuesVisitor extends Visitor {
    
    private PrintWriter writer;
    
    /**
     * Constucts the visitor.
     * @param writer the output stream to write the code to
     */
    public GenerateValuesVisitor(Writer writer) {
        this.writer = new PrintWriter(writer, true);
    }

    /**
     * Writes the list of format sets separated by commas.
     * @param disassembler the disassembler node
     * @throws SemanticException never
     */
    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        Iterator<TreeNode> formats = disassembler.getChildren().iterator();
        
        while (formats.hasNext()) {
            formats.next().accept(this);
            
            if (formats.hasNext())
                writer.println(", ");
        }
    }
    
    /**
     * Writes the list of values separated by commas and enclosed in curly
     * brackets.
     * @param format the format node
     * @throws SemanticException never
     */
    @Override
    public void visit(Format format) throws SemanticException {
        writer.print("{");
        Iterator<TreeNode> values = format.getChildren().iterator();
        
        while (values.hasNext()) {
            values.next().accept(this);
            
            if (values.hasNext())
                writer.print(", ");
        }
        
        writer.print("}");
    }

    /**
     * Writes the name of the field for the disassembler value.
     * @param value the value node
     * @throws SemanticException never
     */
    @Override
    public void visit(Value value) throws SemanticException {
        writer.print(value.getFieldName());
    }
    
}
