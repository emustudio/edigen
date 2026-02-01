/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.ui;

/**
 * The command line help text generator.
 */
public class Help {
    private final String executionCommand;
    private final CommandLine commandLine;

    /**
     * Constructs the help generator.
     *
     * @param executionCommand the command to execute this application
     * @param commandLine      the command line argument parser object
     */
    public Help(String executionCommand, CommandLine commandLine) {
        this.executionCommand = executionCommand;
        this.commandLine = commandLine;
    }

    /**
     * Generates the help text.
     *
     * @return the help text
     */
    public String generate() {
        StringBuilder help = new StringBuilder("Usage:\n ");
        help.append(executionCommand);

        generateMandatory(help);
        generateOptional(help);

        return help.toString();
    }

    /**
     * Generates the help for all mandatory arguments.
     *
     * @param help the string builder to append the text to
     */
    private void generateMandatory(StringBuilder help) {
        for (Argument argument : commandLine.getMandatoryArguments()) {
            help.append(' ').append(argument.getValue());
        }

        help.append(" [options...]\n\n");

        for (Argument argument : commandLine.getMandatoryArguments()) {
            help.append(' ').append(argument.getDescription()).append('\n');
        }

        help.append('\n');
    }

    /**
     * Generates the help for all optional arguments.
     *
     * @param help the string builder to append the text to
     */
    private void generateOptional(StringBuilder help) {
        help.append("Supported options:\n");

        for (Argument argument : commandLine.getOptionalArguments()) {
            String definition = argument.getOption();

            if (argument.getType() == Argument.Type.VALUE) {
                definition += ' ' + argument.getValue();
            }

            String line = String.format(" -%-15s %s", definition, argument.getDescription());
            help.append(line).append('\n');
        }
    }
}
