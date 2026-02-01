/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
        String lineSeparator = System.lineSeparator();

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
