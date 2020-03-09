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
package net.emustudio.edigen;

import net.emustudio.edigen.nodes.TreeNode;

/**
 * This class represents an error found during semantic analysis, for example
 * a duplicate rule name.
 * @author Matúš Sulír
 */
public class SemanticException extends Exception {

    /**
     * Constructs a semantic exception.
     * @param message the message accurately describing the error
     * @param node the affected node, used to display a line number
     */
    public SemanticException(String message, TreeNode node) {
        super(((node.getLine() != null) ? "Line " + node.getLine() + ": " : "")
                + message);
    }
    
}
