/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.parser.ParseException;
import net.emustudio.edigen.ui.Argument;
import net.emustudio.edigen.ui.CommandLine;
import net.emustudio.edigen.ui.CommandLineException;
import net.emustudio.edigen.ui.Help;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static net.emustudio.edigen.Setting.*;

/**
 * The main application class.
 */
public class Edigen {

    private final GenerationLog log;

    /**
     * Constructs an invocation using the caller's logger.
     *
     * @param log receives progress, warnings and lazy debug messages
     */
    public Edigen(GenerationLog log) {
        this.log = log;
    }

    private static final Argument[] ARGUMENTS = {
            new Argument("<specification> - File containing the description of instructions",
                    SPECIFICATION),
            new Argument("<decoder> - Resulting instruction decoder package + class name",
                    DECODER_NAME),
            new Argument("<disassembler> - Resulting disassembler package + class name",
                    DISASSEMBLER_NAME),
            new Argument("ao", "Write generated disassembler file to <directory>",
                    DISASSEMBLER_DIRECTORY),
            new Argument("at", "Use <template> for disassembler instead of the default one",
                    DISASSEMBLER_TEMPLATE),
            new Argument("d", "Enable debug mode", DEBUG),
            new Argument("iu", "Ignore unused rules", IGNORE_UNUSED_RULES),
            new Argument("do", "Write generated decoder file to <directory>", DECODER_DIRECTORY),
            new Argument("dt", "Use <template> for decoder instead of the default one",
                    DECODER_TEMPLATE)
    };

    /**
     * The application entry point used when running the program from the
     * command line.
     * <p>
     * Displays information or error messages and calls the translator.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean success = false;

        try {
            new Edigen(new ConsoleGenerationLog(System.err, Arrays.asList(args).contains("-d"))).run(args);
            success = true;
        } catch (CommandLineException ex) {
            if (args.length == 0)
                success = true;
            else
                System.err.println("ERROR " + ex.getMessage());

            Help help = new Help("java -jar edigen.jar", new CommandLine(ARGUMENTS));
            System.out.print(help.generate());
        } catch (IOException | ParseException | SemanticException ex) {
            System.err.println("ERROR " + ex.getMessage());
        } finally {
            if (!success)
                System.exit(1);
        }
    }

    /**
     * Runs the generator without calling System.exit() - useful when running
     * from a Maven plugin.
     *
     * @param args the arguments
     * @throws CommandLineException when the arguments are invalid
     * @throws IOException          when an I/O error occurs
     * @throws ParseException       when the input file can not be parsed
     * @throws SemanticException    when the input file is semantically invalid
     */
    public void run(String[] args) throws CommandLineException, IOException,
            ParseException, SemanticException {
        CommandLine commandLine = new CommandLine(ARGUMENTS);
        Map<Setting, String> configuration = commandLine.parse(args);
        new Translator(configuration, log).translate();
    }
}
