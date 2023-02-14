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
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

public class SemanticCheckVisitorTest {
    private Decoder decoder;
    private Variant variant;
    private Disassembler disassembler;
    private Format format;

    @Before
    public void setUp() {
        decoder = new Decoder();
        Rule rule = new Rule("rule");
        decoder.addChild(rule);
        variant = new Variant();
        rule.addChild(variant);

        disassembler = new Disassembler();
        format = new Format("");
        disassembler.addChild(format);
    }

    @Test(expected = SemanticException.class)
    public void testNotEndingSubruleMustHaveLengthSpecified() throws SemanticException {
        // rule = subrule1 subrule2 ;
        variant.addChild(new Subrule("subrule1"));
        variant.addChild(new Subrule("subrule2"));
        decoder.accept(new SemanticCheckVisitor());
    }

    @Test
    public void testEndingSubruleDoesntNeedToHaveLengthSpecified() throws SemanticException {
        // rule = subrule1 ;
        variant.addChild(new Subrule("subrule1"));
        decoder.accept(new SemanticCheckVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testNonReturningValueUsedInDisassemblerThrows() throws SemanticException {
        // "" = rule
        Value value = new Value("rule");
        format.addChild(value);
        Specification specification = new Specification(decoder, disassembler);
        specification.accept(new ResolveNamesVisitor());
        specification.accept(new SemanticCheckVisitor());
    }

    @Test
    public void testReturningValueCanBeUsedInDisassembler() throws SemanticException {
        // rule = "at least something": ;
        // %%
        // "" = rule
        Value value = new Value("rule");
        format.addChild(value);
        variant.setReturnString("at least something");
        Specification specification = new Specification(decoder, disassembler);
        specification.accept(new ResolveNamesVisitor());
        specification.accept(new SemanticCheckVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testTwoValuesUsedInFormatCannotBeTheSame() throws SemanticException {
        // rule = "at least something": ;
        // %%
        // "" = rule
        // "" = rule
        Value value = new Value("rule");
        format.addChild(value);

        Format format2 = new Format("");
        format2.addChild(value);
        disassembler.addChild(format2);

        variant.setReturnString("at least something");
        Specification specification = new Specification(decoder, disassembler);
        specification.accept(new ResolveNamesVisitor());
        specification.accept(new SemanticCheckVisitor());
    }
}
