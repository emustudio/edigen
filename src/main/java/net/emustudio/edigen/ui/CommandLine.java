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
package net.emustudio.edigen.ui;

import net.emustudio.edigen.Setting;

import java.util.*;

/**
 * The command line argument parser.
 */
public class CommandLine {

    private final List<Argument> mandatory = new ArrayList<Argument>();
    private final Map<String, Argument> optional = new LinkedHashMap<String, Argument>();
    private Map<Setting, String> configuration;
    
    /**
     * Constructs the command line parser for the given list of possible
     * arguments, their types, etc.
     * @param arguments the argument specification
     */
    public CommandLine(Argument[] arguments) {
        for (Argument argument : arguments) {
            if (argument.getType() == Argument.Type.MANDATORY) {
                mandatory.add(argument);
            } else {
                optional.put(argument.getOption(), argument);
            }
        }
    }

    /**
     * Parses the given argument list.
     * @param arguments the argument list, as obtained from the
     *        <code>main()</code> method
     * @return the configuration object (a set of key-value pairs)
     * @throws CommandLineException when the arguments are invalid
     */
    public Map<Setting, String> parse(String[] arguments) throws CommandLineException {
        configuration = new EnumMap<Setting, String>(Setting.class);
        Iterator<Argument> expectedMandatory = mandatory.iterator();
        Iterator<String> allArguments = Arrays.asList(arguments).iterator();
        
        while (allArguments.hasNext()) {
            String currentArgument = allArguments.next();
        
            if (currentArgument.startsWith("-")) {
                parseOptional(currentArgument, allArguments);
            } else {
                parseMandatory(currentArgument, expectedMandatory);
            }
        }
        
        if (expectedMandatory.hasNext())
            throw new CommandLineException("Too few arguments given");
        
        return configuration;
    }
    
    /**
     * Returns a list of all expected mandatory arguments.
     * @return the iterable list
     */
    public Iterable<Argument> getMandatoryArguments() {
        return mandatory;
    }

    /**
     * Returns a list of all possible optional arguments.
     * @return the iterable list
     */
    public Iterable<Argument> getOptionalArguments() {
        return optional.values();
    }
    
    /**
     * Parses a named optional argument - a flag or a parameter with a value.
     * @param current the current argument
     * @param all the iterator to all arguments, set to the current one
     * @throws CommandLineException when the option is unknown or the value
     *         argument does not have a value
     */
    private void parseOptional(String current, Iterator<String> all) throws CommandLineException  {
        String option = current.substring(1);
        Argument argument = optional.get(option);

        if (argument != null) {
            if (argument.getType() == Argument.Type.FLAG) {
                configuration.put(argument.getKey(), "");
            } else {
                if (all.hasNext() && !(current = all.next()).startsWith("-")) {
                    configuration.put(argument.getKey(), current);
                } else {
                    throw new CommandLineException("Option \"-" + option + "\" does not have a value");
                }
            }
        } else {
            throw new CommandLineException("Unknown option \"-" + option + "\"");
        }
    }
    
    /**
     * Parses an mandatory argument.
     * @param argument the current argument
     * @param expected the iterator of manatory arguments, set to the current
     *        one
     * @throws CommandLineException when too many arguments are given
     */
    private void parseMandatory(String argument, Iterator<Argument> expected)
            throws CommandLineException {
        if (expected.hasNext())
            configuration.put(expected.next().getKey(), argument);
        else
            throw new CommandLineException("Too many arguments given");
    }
}
