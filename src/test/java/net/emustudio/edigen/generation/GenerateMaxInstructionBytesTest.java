/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.Specification;
import net.emustudio.edigen.parser.ParseException;
import net.emustudio.edigen.parser.Parser;
import net.emustudio.edigen.passes.*;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class GenerateMaxInstructionBytesTest {

    @Test
    public void testNoSubruleReferences() throws ParseException, SemanticException {
        String maxBytes = generateMaxBytes(
                "root instruction;\n" +
                        "instruction = \"a\": 0x00 0x01 | \"b\": 0x10;\n" +
                        "%%\n" +
                        "\"%s\" = instruction;");
        assertEquals("2", maxBytes);
    }

    @Test
    public void testSubruleReference() throws ParseException, SemanticException {
        String maxBytes = generateMaxBytes(
                "root instruction;\n" +
                        "instruction = \"a\": 0x00 other | \"b\": 0x10;\n" +
                        "other = 0x11 ref16;\n" +
                        "ref16 = ref16: ref16(16);\n" +
                        "%%\n" +
                        "\"%s\" = instruction;");
        assertEquals("4", maxBytes);
    }

    @Test
    public void testMultipleSubruleReference() throws ParseException, SemanticException {
        String maxBytes = generateMaxBytes(
                "root instruction;\n" +
                        "instruction = \"a\": 0xFF other | 0x20 bother;\n" +
                        "other = 0x11 ref16;\n" +
                        "bother = ref16(16) other;\n" +
                        "ref16 = ref16: ref16(16);\n" +
                        "%%\n" +
                        "\"%s\" = instruction ref16;");
        assertEquals("6", maxBytes);
    }

    private String generateMaxBytes(String input) throws ParseException, SemanticException {
        Parser parser = new Parser(new StringReader(input));
        Specification specification = parser.parse();
        transform(specification);

        StringWriter writer = new StringWriter();
        new GenerateMaxInstructionBytes(writer).visit(specification.getDecoder());

        return writer.toString();
    }

    private void transform(Specification specification) throws SemanticException {
        Visitor[] transforms = {
                new ResolveNamesVisitor(),
                new DetectRootRulesVisitor(),
                new DetectUnusedRulesVisitor(),
                new SemanticCheckVisitor(),
                new MergePatternsVisitor(),
                new SortVisitor(),
                new SplitVisitor(),
                new PushDownVariantsVisitor(),
                new GroupVisitor(),
                new DetectAmbiguousVisitor(),
                new NarrowMasksVisitor(),
                new RemoveUnreachablePatternsVisitor(),
                new DetectUnreachableFormatsVisitor()
        };

        for (Visitor visitor : transforms) {
            specification.accept(visitor);
        }
    }
}
