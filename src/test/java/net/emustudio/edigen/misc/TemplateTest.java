/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.misc;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class TemplateTest {

    @Test
    public void testSetAndReplaceInlineVariable() throws IOException {
        String input = "Hello %name%!";
        String expected = "Hello World!" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("name", "World"));
        assertEquals(expected, result);
    }

    @Test
    public void testMultipleInlineVariables() throws IOException {
        String input = "%greeting% %name%!";
        String expected = "Hello World!" + System.lineSeparator();
        
        String result = runTemplate(input, t -> {
            t.setVariable("greeting", "Hello");
            t.setVariable("name", "World");
        });
        assertEquals(expected, result);
    }

    @Test
    public void testBlockVariable() throws IOException {
        String input = "    %code%";
        String value = "int x = 5;\nint y = 10;";
        String expected = "    int x = 5;\n    int y = 10;" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("code", value));
        assertEquals(expected, result);
    }

    @Test
    public void testBlockVariableWithDifferentIndentation() throws IOException {
        String input = "        %method%";
        String value = "public void test() {\n    return;\n}";
        String expected = "        public void test() {\n            return;\n        }" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("method", value));
        assertEquals(expected, result);
    }

    @Test
    public void testUnsetVariableIsLeftUnmodified() throws IOException {
        String input = "Hello %name%!";
        String expected = "Hello %name%!" + System.lineSeparator();
        
        String result = runTemplate(input, t -> {});
        assertEquals(expected, result);
    }

    @Test
    public void testMixedSetAndUnsetVariables() throws IOException {
        String input = "%greeting% %name%!";
        String expected = "Hello %name%!" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("greeting", "Hello"));
        assertEquals(expected, result);
    }

    @Test
    public void testMultipleLines() throws IOException {
        String input = "Line 1: %var1%\nLine 2: %var2%";
        String lineSep = System.lineSeparator();
        String expected = "Line 1: A" + lineSep + "Line 2: B" + lineSep;
        
        String result = runTemplate(input, t -> {
            t.setVariable("var1", "A");
            t.setVariable("var2", "B");
        });
        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVariableNameStartingWithNumber() throws IOException {
        runTemplate("test", t -> t.setVariable("1invalid", "value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVariableNameWithSpecialCharacters() throws IOException {
        runTemplate("test", t -> t.setVariable("invalid-name", "value"));
    }

    @Test
    public void testValidVariableNameWithUnderscore() throws IOException {
        String input = "%my_var%";
        String expected = "value" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("my_var", "value"));
        assertEquals(expected, result);
    }

    @Test
    public void testValidVariableNameStartingWithUnderscore() throws IOException {
        String input = "%_var%";
        String expected = "value" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("_var", "value"));
        assertEquals(expected, result);
    }

    @Test
    public void testValidVariableNameWithNumbers() throws IOException {
        String input = "%var123%";
        String expected = "value" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("var123", "value"));
        assertEquals(expected, result);
    }

    @Test
    public void testEmptyTemplate() throws IOException {
        String input = "";
        String result = runTemplate(input, t -> {});
        assertEquals("", result);
    }

    @Test
    public void testEmptyVariableValue() throws IOException {
        String input = "Start %empty% End";
        String expected = "Start  End" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("empty", ""));
        assertEquals(expected, result);
    }

    @Test
    public void testSameVariableMultipleTimes() throws IOException {
        String input = "%var% and %var%";
        String expected = "test and test" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("var", "test"));
        assertEquals(expected, result);
    }

    @Test
    public void testBlockVariableWithNoIndentation() throws IOException {
        String input = "%code%";
        String value = "line1\nline2";
        String expected = "line1\nline2" + System.lineSeparator();
        
        String result = runTemplate(input, t -> t.setVariable("code", value));
        assertEquals(expected, result);
    }

    private String runTemplate(String input, TemplateSetup setup) throws IOException {
        StringWriter outputWriter = new StringWriter();
        BufferedReader reader = new BufferedReader(new StringReader(input));
        BufferedWriter writer = new BufferedWriter(outputWriter);
        
        Template template = new Template(reader, writer);
        setup.setup(template);
        template.write();
        writer.flush();
        
        return outputWriter.toString();
    }
    
    @FunctionalInterface
    private interface TemplateSetup {
        void setup(Template template) throws IOException;
    }
}

