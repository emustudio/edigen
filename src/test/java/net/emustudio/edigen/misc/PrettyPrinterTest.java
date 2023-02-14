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

import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Test of the PrettyPrinter class.
 */
public class PrettyPrinterTest {

    /**
     * Test of writeLine method, of class PrettyPrinter.
     */
    @Test
    public void testWriteLine() {
        String[] input = {
            "if (a) {",
            "switch (b) {",
            "case 1:",
            "break;",
            "default:",
            "}",
            "} else {",
            "}"
        };

        String[] expectedLines = {
            "if (a) {",
            "    switch (b) {",
            "    case 1:",
            "        break;",
            "    default:",
            "    }",
            "} else {",
            "}"
        };

        StringBuilder expected = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");

        for (String line : expectedLines) {
            expected.append(line).append(lineSeparator);
        }

        StringWriter output = new StringWriter();
        PrettyPrinter printer = new PrettyPrinter(output);

        for (String line : input) {
            printer.writeLine(line);
        }

        assertEquals(expected.toString(), output.toString());
    }
}
