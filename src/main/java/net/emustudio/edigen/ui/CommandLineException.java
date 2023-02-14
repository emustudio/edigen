/*
 * Copyright (C) 2011-2022 Matúš Sulír, Peter Jakubčo
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
package net.emustudio.edigen.ui;

/**
 * An exception thrown when the command line arguments are invalid.
 */
public class CommandLineException extends Exception {

    /**
     * Constructs the exception.
     * @param message the specific message
     */
    public CommandLineException(String message) {
        super(message);
    }
    
}
