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

/**
 * An enumeration of possible program configuration settings, usually read from the command line.
 */
public enum Setting {
    /**
     * A specification file.
     */
    SPECIFICATION,
    /**
     * The package + class name of the generated instruction decoder.
     */
    DECODER_NAME,
    /**
     * The package + class name of the generated disassembler.
     */
    DISASSEMBLER_NAME,
    /**
     * An output directory of the generated disassembler file.
     */
    DISASSEMBLER_DIRECTORY,
    /**
     * The generated disassembler will be a member of this package.
     */
    DISASSEMBLER_PACKAGE,
    /**
     * The external disassembler template to use (instead of the internal, default template).
     */
    DISASSEMBLER_TEMPLATE,
    /**
     * If set, the program will run in the debug mode (printing the tree after each transformation).
     */
    DEBUG,
    /**
     * An output directory of the generated instruction decoder file.
     */
    DECODER_DIRECTORY,
    /**
     * The generated decoder will be a member of this package.
     */
    DECODER_PACKAGE,
    /**
     * The external decoder template to use (instead of the internal, default template).
     */
    DECODER_TEMPLATE,
    /**
     * Ignore unused rules. If enabled, detection of unused rules will be turned off.
     */
    IGNORE_UNUSED_RULES
}
