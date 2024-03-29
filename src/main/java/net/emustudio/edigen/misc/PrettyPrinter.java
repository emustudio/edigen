/*
 * This file is part of edigen.
 *
 * Copyright (C) 2011-2023 Matúš Sulír, Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.edigen.misc;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Minimalistic on-the-fly Java source code pretty-printer.
 *
 * <p>
 * The input must conform to some limitations, mainly:
 * <ul>
 * <li>The "prettifier" provides only indentation. The input must already be
 * split correctly into lines according to the standard Java source code
 * style.</li>
 * <li>Each block (like the body of an <code>if</code> or <code>while</code>
 * statement) must be enclosed in brackets.</li>
 * </ul>
 * <p>
 * Example of a valid input:
 * <pre>
 * if (something) {
 * switch(a) {
 * case 1:
 * break;
 * }
 * } else {
 * }
 * </pre>
 * <p>
 * This produces the output:
 * <pre>
 * if (something) {
 *     switch(a) {
 *     case 1:
 *         break;
 *     }
 * } else {
 * }
 * </pre>
 *
 */
public class PrettyPrinter {

    private final PrintWriter output;
    private int indentCount = 0;
    private static final String indentString = "    ";

    /**
     * Constructs the pretty printer.
     *
     * @param output the output to write the source code to
     */
    public PrettyPrinter(Writer output) {
        this.output = new PrintWriter(output, true);
    }

    /**
     * Indents the line as necessary and writes it to the output stream.
     *
     * @param text the line of text
     */
    public void writeLine(String text) {
        if (text.startsWith("}") || text.endsWith(":"))
            unindent();

        printIndentation();
        output.println(text);

        if (text.endsWith("{") || text.endsWith(":"))
            indent();
    }

    /**
     * Writes text to the output stream as-is.
     *
     * @param text text to write
     */
    public void write(String text) {
        output.print(text);
    }

    /**
     * Makes the indentation of the next written line bigger.
     */
    private void indent() {
        indentCount++;
    }

    /**
     * Makes the indentation of the next written line smaller.
     */
    private void unindent() {
        if (indentCount > 0) {
            indentCount--;
        }
    }

    /**
     * Writes the curently set indentation to the stream.
     */
    private void printIndentation() {
        for (int i = 0; i < indentCount; i++)
            output.print(indentString);
    }
}
