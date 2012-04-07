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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A command line argument specification.
 * 
 * This does not represent a specific argument instance obtained from the user,
 * but information about the possible argument, like his name and description.
 * @author Matúš Sulír
 */
public class Argument {

    /**
     * An argument type.
     * 
     * <p>If <tt>arg1</tt> is a mandatory argument, <tt>-arg2 value2</tt> is a
     * value and <tt>-arg3</tt> is a flag, this is an example of a valid
     * command line input:
     * <pre><kbd>program arg1 -arg2 value2 -arg3</kbd></pre>
     * The order of value and flag arguments is irrelevant. Mandatory arguments
     * must occur in the specified order, but they can interleave with other
     * parameters.</p>
     */
    public enum Type {
        MANDATORY,
        VALUE,
        FLAG
    }
    
    private static final Pattern HELP_VALUE = Pattern.compile("<\\w+>");
    
    private Type type;
    private String option;
    private String value;
    private String description;
    private String key;

    /**
     * Constructs an unnamed mandatory argument.
     * @param description the description used in the help text
     * @param key the key to which the recognized value will be later assigned
     */
    public Argument(String description, String key) {
        type = Type.MANDATORY;
        option = "";
        
        Matcher matcher = HELP_VALUE.matcher(description);

        if (matcher.find())
            value = matcher.group();
        else
            value = "<value>";
        
        this.description = description;
        this.key = key;
    }
    
    /**
     * Constructs a flag or a value argument.
     * @param option the option name, e.g. "o" for "-o"
     * @param description the description used in the help text
     * @param key the key to which the recognized value will be later assigned
     */
    public Argument(String option, String description, String key) {
        Matcher matcher = HELP_VALUE.matcher(description);
        
        if (matcher.find()) {
            type = Type.VALUE;
            value = matcher.group();
        } else {
            type = Type.FLAG;
            value = "";
        }
        
        this.option = option;
        this.description = description;
        this.key = key;
    }

    /**
     * Returns the argument type.
     * @return the type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns the option name, e.g. "o" for "-o"
     * @return the option name, or an empty string if the argument is unnamed
     */
    public String getOption() {
        return option;
    }

    /**
     * Returns the value name suitable for printing in a help text,
     * e.g. "<directory>". The value is automatically extracted from the
     * description.
     * @return the value including the brackets; "<value>" in case of extraction
     *         failure for a mandatory argument; an empty string for a flag
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Returns the description used in the help text.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the key to which the recognized value will be later assigned.
     * @return the key
     */
    public String getKey() {
        return key;
    }
}
