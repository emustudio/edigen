/*
 * Copyright (C) 2011 Matúš Sulír
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
package edigen.util;

import edigen.tree.SimpleNode;
import java.io.PrintStream;

/**
 * The recursive tree printer.
 * @author Matúš Sulír
 */
public class TreePrinter {
    
    private PrintStream outStream;
    
    /**
     * Constructs the tree printer.
     * @param output the stream to write to
     */
    public TreePrinter(PrintStream output) {
        this.outStream = output;
    }
    
    /**
     * Prints a tree recursively.
     * 
     * Starts with a zero indentation. Node values are printed if they are
     * present.
     * 
     * @param node the root node to dump
     */
    public void dump(SimpleNode node) {
        print(node, 0);
        outStream.println("---------------");
    }
    
    /**
     * Prints a tree node recursively.
     * 
     * If a node contains a value, it is also printed.
     * 
     * @param node the node to dump
     * @param indentCount the intentation level (to visually represent the tree)
     */
    private void print(SimpleNode node, int indentCount) {
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < indentCount; i++)
            output.append("  ");
        
        output.append(node);
        
        if (node.jjtGetValue() != null)
            output.append(": ").append(node.jjtGetValue());
        
        outStream.println(output);

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
            print((SimpleNode) node.jjtGetChild(i), indentCount + 1);
    }
    
}
