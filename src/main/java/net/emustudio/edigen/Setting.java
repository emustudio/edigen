/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
