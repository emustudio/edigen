/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.ui;

import net.emustudio.edigen.Setting;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class CommandLineTest {

    private CommandLine commandLine;

    @Before
    public void setUp() {
        Argument[] arguments = new Argument[]{
                new Argument("Input specification <file>", Setting.SPECIFICATION),
                new Argument("d", "Enable debug mode", Setting.DEBUG),
                new Argument("o", "Output <directory>", Setting.DECODER_DIRECTORY),
                new Argument("p", "Package <name>", Setting.DECODER_PACKAGE)
        };
        commandLine = new CommandLine(arguments);
    }

    @Test
    public void testParseMandatoryArgument() throws CommandLineException {
        Map<Setting, String> config = commandLine.parse(new String[]{"spec.txt"});

        assertEquals("spec.txt", config.get(Setting.SPECIFICATION));
        assertEquals(1, config.size());
    }

    @Test
    public void testParseFlagArgument() throws CommandLineException {
        Map<Setting, String> config = commandLine.parse(new String[]{"spec.txt", "-d"});

        assertEquals("spec.txt", config.get(Setting.SPECIFICATION));
        assertEquals("", config.get(Setting.DEBUG));
        assertEquals(2, config.size());
    }

    @Test
    public void testParseValueArgument() throws CommandLineException {
        Map<Setting, String> config = commandLine.parse(new String[]{"spec.txt", "-o", "output"});

        assertEquals("spec.txt", config.get(Setting.SPECIFICATION));
        assertEquals("output", config.get(Setting.DECODER_DIRECTORY));
        assertEquals(2, config.size());
    }

    @Test
    public void testParseMultipleArguments() throws CommandLineException {
        Map<Setting, String> config = commandLine.parse(new String[]{
            "spec.txt", "-d", "-o", "output", "-p", "com.example"
        });

        assertEquals("spec.txt", config.get(Setting.SPECIFICATION));
        assertEquals("", config.get(Setting.DEBUG));
        assertEquals("output", config.get(Setting.DECODER_DIRECTORY));
        assertEquals("com.example", config.get(Setting.DECODER_PACKAGE));
        assertEquals(4, config.size());
    }

    @Test
    public void testParseMandatoryInterleavedWithOptional() throws CommandLineException {
        Map<Setting, String> config = commandLine.parse(new String[]{
            "-d", "spec.txt", "-o", "output"
        });

        assertEquals("spec.txt", config.get(Setting.SPECIFICATION));
        assertEquals("", config.get(Setting.DEBUG));
        assertEquals("output", config.get(Setting.DECODER_DIRECTORY));
    }

    @Test(expected = CommandLineException.class)
    public void testTooFewArguments() throws CommandLineException {
        commandLine.parse(new String[]{});
    }

    @Test(expected = CommandLineException.class)
    public void testTooManyArguments() throws CommandLineException {
        commandLine.parse(new String[]{"spec.txt", "extra.txt"});
    }

    @Test(expected = CommandLineException.class)
    public void testUnknownOption() throws CommandLineException {
        commandLine.parse(new String[]{"spec.txt", "-x"});
    }

    @Test(expected = CommandLineException.class)
    public void testValueOptionWithoutValue() throws CommandLineException {
        commandLine.parse(new String[]{"spec.txt", "-o"});
    }

    @Test(expected = CommandLineException.class)
    public void testValueOptionWithOptionAsValue() throws CommandLineException {
        commandLine.parse(new String[]{"spec.txt", "-o", "-d"});
    }

    @Test
    public void testGetMandatoryArguments() {
        int count = 0;
        for (Argument arg : commandLine.getMandatoryArguments()) {
            assertEquals(Argument.Type.MANDATORY, arg.getType());
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testGetOptionalArguments() {
        int count = 0;
        for (Argument arg : commandLine.getOptionalArguments()) {
            assertNotEquals(Argument.Type.MANDATORY, arg.getType());
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testEmptyCommandLine() {
        CommandLine emptyCmdLine = new CommandLine(new Argument[]{});

        int mandatoryCount = 0;
        for (Argument arg : emptyCmdLine.getMandatoryArguments()) {
            mandatoryCount++;
        }
        assertEquals(0, mandatoryCount);

        int optionalCount = 0;
        for (Argument arg : emptyCmdLine.getOptionalArguments()) {
            optionalCount++;
        }
        assertEquals(0, optionalCount);
    }

    @Test
    public void testParseOnlyFlags() throws CommandLineException {
        Argument[] flagsOnly = new Argument[] {
            new Argument("Input <file>", Setting.SPECIFICATION),
            new Argument("d", "Debug", Setting.DEBUG),
            new Argument("u", "Ignore unused", Setting.IGNORE_UNUSED_RULES)
        };
        CommandLine cmd = new CommandLine(flagsOnly);

        Map<Setting, String> config = cmd.parse(new String[]{"spec.txt", "-d", "-u"});

        assertEquals("spec.txt", config.get(Setting.SPECIFICATION));
        assertEquals("", config.get(Setting.DEBUG));
        assertEquals("", config.get(Setting.IGNORE_UNUSED_RULES));
    }

    @Test
    public void testExceptionWithMessage() {
        String message = "Invalid command line argument";
        CommandLineException exception = new CommandLineException(message);

        assertEquals(message, exception.getMessage());
    }
}
