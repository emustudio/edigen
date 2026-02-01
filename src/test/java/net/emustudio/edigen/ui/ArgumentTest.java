/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.ui;

import net.emustudio.edigen.Setting;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArgumentTest {

    @Test
    public void testMandatoryArgumentCreation() {
        Argument arg = new Argument("Input <file>", Setting.SPECIFICATION);
        
        assertEquals(Argument.Type.MANDATORY, arg.getType());
        assertEquals("", arg.getOption());
        assertEquals("<file>", arg.getValue());
        assertEquals("Input <file>", arg.getDescription());
        assertEquals(Setting.SPECIFICATION, arg.getKey());
    }

    @Test
    public void testMandatoryArgumentWithoutBrackets() {
        Argument arg = new Argument("Input file", Setting.SPECIFICATION);
        
        assertEquals(Argument.Type.MANDATORY, arg.getType());
        assertEquals("<value>", arg.getValue()); // Default when no brackets found
    }

    @Test
    public void testFlagArgumentCreation() {
        Argument arg = new Argument("d", "Enable debug mode", Setting.DEBUG);
        
        assertEquals(Argument.Type.FLAG, arg.getType());
        assertEquals("d", arg.getOption());
        assertEquals("", arg.getValue());
        assertEquals("Enable debug mode", arg.getDescription());
        assertEquals(Setting.DEBUG, arg.getKey());
    }

    @Test
    public void testValueArgumentCreation() {
        Argument arg = new Argument("o", "Output <directory>", Setting.DECODER_DIRECTORY);
        
        assertEquals(Argument.Type.VALUE, arg.getType());
        assertEquals("o", arg.getOption());
        assertEquals("<directory>", arg.getValue());
        assertEquals("Output <directory>", arg.getDescription());
        assertEquals(Setting.DECODER_DIRECTORY, arg.getKey());
    }

    @Test
    public void testValueExtractionFromDescription() {
        Argument arg1 = new Argument("p", "Package <name>", Setting.DECODER_PACKAGE);
        assertEquals("<name>", arg1.getValue());
        
        Argument arg2 = new Argument("t", "Template <file_path>", Setting.DECODER_TEMPLATE);
        assertEquals("<file_path>", arg2.getValue());
    }

    @Test
    public void testMultipleWordsInBrackets() {
        Argument arg = new Argument("Output <some_value>", Setting.DISASSEMBLER_DIRECTORY);
        assertEquals("<some_value>", arg.getValue());
    }
}
