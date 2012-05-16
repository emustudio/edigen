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
package edigen.ui;

/**
 * The command line help text generator.
 * @author Matúš Sulír
 */
public class Help {
    private String executionCommand;
    private CommandLine commandLine;

    /**
     * Consturcts the help generator.
     * @param executionCommand the command to execute this application
     * @param commandLine the command line argument parser object
     */
    public Help(String executionCommand, CommandLine commandLine) {
        this.executionCommand = executionCommand;
        this.commandLine = commandLine;
    }
    
    /**
     * Generates the help text.
     * @return the help text
     */
    public String generate() {
        StringBuilder help = new StringBuilder("Usage: ");
        help.append(executionCommand);
        
        generateMandatory(help);
        generateOptional(help);
        
        return help.toString();
    }
    
    /**
     * Generates the help for all mandatory arguments.
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
