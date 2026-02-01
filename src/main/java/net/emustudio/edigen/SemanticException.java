/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.nodes.TreeNode;

/**
 * This class represents an error found during semantic analysis, for example
 * a duplicate rule name.
 */
public class SemanticException extends Exception {

    /**
     * Constructs a semantic exception.
     *
     * @param message the message accurately describing the error
     * @param node    the affected node, used to display a line number
     */
    public SemanticException(String message, TreeNode node) {
        super(((node.getLine() != null) ? "Line " + node.getLine() + ": " : "")
                + message);
    }
}
