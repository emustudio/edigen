/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.ui;

import net.emustudio.edigen.Setting;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HelpTest {
    
    private CommandLine commandLine;
    private Help help;
    
    @Before
    public void setUp() {
        Argument[] arguments = new Argument[] {
            new Argument("Input specification <file>", Setting.SPECIFICATION),
            new Argument("Output <file>", Setting.DECODER_NAME),
            new Argument("d", "Enable debug mode", Setting.DEBUG),
            new Argument("o", "Output <directory>", Setting.DECODER_DIRECTORY),
            new Argument("p", "Package <name>", Setting.DECODER_PACKAGE)
        };
        commandLine = new CommandLine(arguments);
        help = new Help("edigen", commandLine);
    }

    @Test
    public void testGenerateContainsUsage() {
        String helpText = help.generate();
        assertTrue(helpText.contains("Usage:"));
    }

    @Test
    public void testGenerateContainsExecutionCommand() {
        String helpText = help.generate();
        assertTrue(helpText.contains("edigen"));
    }

    @Test
    public void testGenerateContainsMandatoryArguments() {
        String helpText = help.generate();
        assertTrue(helpText.contains("<file>"));
        assertTrue(helpText.contains("Input specification <file>"));
        assertTrue(helpText.contains("Output <file>"));
    }

    @Test
    public void testGenerateContainsOptions() {
        String helpText = help.generate();
        assertTrue(helpText.contains("Supported options:"));
    }

    @Test
    public void testGenerateContainsFlagOption() {
        String helpText = help.generate();
        assertTrue(helpText.contains("-d"));
        assertTrue(helpText.contains("Enable debug mode"));
    }

    @Test
    public void testGenerateContainsValueOptions() {
        String helpText = help.generate();
        assertTrue(helpText.contains("-o <directory>"));
        assertTrue(helpText.contains("Output <directory>"));
        assertTrue(helpText.contains("-p <name>"));
        assertTrue(helpText.contains("Package <name>"));
    }

    @Test
    public void testGenerateStructure() {
        String helpText = help.generate();
        
        // Check structure: Usage line comes first
        int usageIndex = helpText.indexOf("Usage:");
        int optionsIndex = helpText.indexOf("Supported options:");
        
        assertTrue(usageIndex >= 0);
        assertTrue(optionsIndex > usageIndex);
    }

    @Test
    public void testGenerateWithCustomCommand() {
        Help customHelp = new Help("java -jar edigen.jar", commandLine);
        String helpText = customHelp.generate();
        
        assertTrue(helpText.contains("java -jar edigen.jar"));
    }

    @Test
    public void testGenerateWithNoOptionalArguments() {
        Argument[] mandatoryOnly = new Argument[] {
            new Argument("Input <file>", Setting.SPECIFICATION)
        };
        CommandLine cmd = new CommandLine(mandatoryOnly);
        Help h = new Help("edigen", cmd);
        
        String helpText = h.generate();
        assertTrue(helpText.contains("Usage:"));
        assertTrue(helpText.contains("Input <file>"));
        assertTrue(helpText.contains("Supported options:"));
    }

    @Test
    public void testGenerateWithNoMandatoryArguments() {
        Argument[] optionalOnly = new Argument[] {
            new Argument("d", "Enable debug mode", Setting.DEBUG)
        };
        CommandLine cmd = new CommandLine(optionalOnly);
        Help h = new Help("edigen", cmd);
        
        String helpText = h.generate();
        assertTrue(helpText.contains("Usage:"));
        assertTrue(helpText.contains("-d"));
        assertTrue(helpText.contains("Enable debug mode"));
    }
}
