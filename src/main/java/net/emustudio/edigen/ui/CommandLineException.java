/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.ui;

/**
 * An exception thrown when the command line arguments are invalid.
 */
public class CommandLineException extends Exception {

    /**
     * Constructs the exception.
     *
     * @param message the specific message
     */
    public CommandLineException(String message) {
        super(message);
    }

}
