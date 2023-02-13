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
        StringReader input = new StringReader(
                "root instruction;\n" +
                        "instruction = \"a\": 0x00 0x01 | \"b\": 0x10;\n" +
                        "%%\n" +
                        "\"%s\" = instruction;");
        Parser parser = new Parser(input);
        Specification specification = parser.parse();
        transform(specification);
        StringWriter writer = new StringWriter();
        new GenerateMaxInstructionBytes(writer).visit(specification.getDecoder());

        assertEquals("2", writer.toString());
    }

    @Test
    public void testSubruleReference() throws ParseException, SemanticException {
        StringReader input = new StringReader(
                "root instruction;\n" +
                        "instruction = \"a\": 0x00 other | \"b\": 0x10;\n" +
                        "other = 0x11 ref16;\n" +
                        "ref16 = ref16: ref16(16);\n" +
                        "%%\n" +
                        "\"%s\" = instruction;");
        Parser parser = new Parser(input);
        Specification specification = parser.parse();
        transform(specification);

        StringWriter writer = new StringWriter();
        new GenerateMaxInstructionBytes(writer).visit(specification.getDecoder());

        assertEquals("4", writer.toString());
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
