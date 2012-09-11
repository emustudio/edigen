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
package edigen;

import edigen.nodes.TreeNode;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * This class represents an error found during semantic analysis, for example
 * a duplicate rule name.
 * @author Matúš Sulír
 */
public class SemanticException extends Exception {
    
    private TreeNode affectedNode = null;
    
    /**
     * Constructs a semantic exception.
     * @param message the message accurately describing the error
     */
    public SemanticException(String message) {
        super(message);
    }

    /**
     * Constructs a semantic exception.
     * @param node affected node
     * @param message the message accurately describing the error
     */
    public SemanticException(TreeNode node, String message) {
        super(message);
        this.affectedNode = node;
    }
    
    private void printIndent(PrintWriter out, int indent) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
    }
    
    private void printTree(PrintWriter out, TreeNode node, int indent) {
        printIndent(out, indent);
        out.println(node.toString());
        for (TreeNode n : node.getChildren()) {
            printTree(out, n, indent + 1);
        }
    }
    
    /**
     * Print string representation of the affected node. If the node is
     * not defined, does nothing.
     */
    public void printAffectedNode(PrintWriter out) {
        if (affectedNode != null) {
            printTree(out, affectedNode, 0);
        }
    }
    
}
