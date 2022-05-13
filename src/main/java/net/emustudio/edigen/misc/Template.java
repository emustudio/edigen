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
package net.emustudio.edigen.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple templating system.
 * 
 * <p>The user supplies a template as a text and a set of variables with their
 * values. In the template, a variable is identified by a percent sign followed
 * by its name. The template system replaces the variables by their values. Each
 * variable can be either block or inline.</p>
 * 
 * <p>A line contains a <em>block variable</em> if it consists only of
 * whitespace characters and the variable itself. During the replacement, each
 * line of the block variable value is prepended with the whitespace string same
 * as that located before the variable name. This allows inserting the source
 * code with the proper indentation.</p>
 * 
 * <p>All other variables are <em>inline</em>. An inline variable is replaced by
 * its value as-is.</p>
 * 
 * <p>Unset variables found in the template are left unmodified.</p>
 */
public class Template {
    
    private static final String VARIABLE_NAME = "([A-Za-z_]\\w*)";
    private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile(VARIABLE_NAME);
    private static final Pattern BLOCK_VARIABLE
            = Pattern.compile("(\\s*)%" + VARIABLE_NAME + "%(\\s*)");
    private static final Pattern INLINE_VARIABLE = Pattern.compile("%" + VARIABLE_NAME + "%");
    private static final Pattern LINE_START = Pattern.compile("^", Pattern.MULTILINE);
    
    private final BufferedReader template;
    private final BufferedWriter output;
    private final Map<String, String> variables = new HashMap<String, String>();
    
    /**
     * Constructs a template system.
     * @param template the input template
     * @param output the writer to write the result to
     */
    public Template(BufferedReader template, BufferedWriter output) {
        this.template = template;
        this.output = output;
    }
    
    /**
     * Sets a value of a variable.
     * @param name the variable name; must start with a letter or an underscore
     *             and continue with letters, numbers and underscores
     * @param value the variable value
     * @throws IllegalArgumentException if the variable name is invalid
     */
    public void setVariable(String name, String value) {
        if (VARIABLE_NAME_PATTERN.matcher(name).matches())
            variables.put(name, value);
        else
            throw new IllegalArgumentException("Invalid variable name");
    }
    
    /**
     * Writes the whole resulting output.
     * @throws IOException if an exception occurs during writing
     */
    public void write() throws IOException {
        String line;
        
        while ((line = template.readLine()) != null) {
            Matcher blockVariable = BLOCK_VARIABLE.matcher(line);
            
            if (blockVariable.matches()) {
                line = replaceBlockVariable(blockVariable);
            } else {
                line = replaceInlineVariables(line);
            }
            
            output.write(line);
            output.newLine();
        }
    }
    
    /**
     * Replaces the block variable with its value (if it was already set).
     * @param matcher the matcher of the whole line containing the block
     *                variable
     * @return the new value of the line
     */
    private String replaceBlockVariable(Matcher matcher) {
        String name = matcher.group(2);
        String value = variables.get(name);
        
        if (value != null) {
            String indentation = matcher.group(1);
            return LINE_START.matcher(value).replaceAll(indentation);
        } else {
            return matcher.group();
        }
    }

    /**
     * Replaces the inline variables with their values (if they were already
     * set).
     * @param line the unmodified line of text
     * @return the new value of the line, with all variables replaced
     */
    private String replaceInlineVariables(String line) {
        Matcher matcher = INLINE_VARIABLE.matcher(line);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = variables.get(name);

            if (value != null) {
                matcher.appendReplacement(result, value);
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
}
