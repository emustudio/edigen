/*
 * Copyright (C) 2016 Matúš Sulír
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
package com.github.sulir.edigen.passes;

import com.github.sulir.edigen.SemanticException;
import com.github.sulir.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test of the ResolveNamesVisitor class.
 * @author Matúš Sulír
 */
public class ResolveNamesVisitorTest {
    private Decoder decoder;
    private Rule rule;
    private Variant variant;
    private Disassembler disassembler;
    private Format format;

    @Before
    public void setUp() {
        decoder = new Decoder();
        rule = new Rule("rule");
        decoder.addChild(rule);
        variant = new Variant();
        rule.addChild(variant);

        disassembler = new Disassembler();
        format = new Format("");
        disassembler.addChild(format);
    }

    @Test
    public void testVariantHasAssociatedSubrule() throws SemanticException {
        variant.setReturnSubrule(new Subrule("subrule"));
        Subrule subrule = new Subrule("subrule", 1, null);
        variant.addChild(subrule);

        decoder.accept(new ResolveNamesVisitor());
        assertEquals(variant.getReturnSubrule(), subrule);
    }

    @Test(expected = SemanticException.class)
    public void testVariantReturnsNonexistentSubrule() throws SemanticException {
        variant.setReturnSubrule(new Subrule("subrule"));

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test
    public void testSubruleHasAssociatedRule() throws SemanticException {
        Subrule subrule = new Subrule("rule2");
        variant.addChild(subrule);
        Rule rule2 = new Rule("rule2");
        decoder.addChild(rule2);

        decoder.accept(new ResolveNamesVisitor());
        assertEquals(subrule.getRule(), rule2);
    }

    @Test(expected = SemanticException.class)
    public void testSubruleRefersToNonexistentRule() throws SemanticException {
        variant.addChild(new Subrule("nonexistent"));

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test
    public void testValueHasAssociatedRule() throws SemanticException {
        Value value = new Value("rule");
        format.addChild(value);

        new Specification(decoder, disassembler).accept(new ResolveNamesVisitor());
        assertEquals(value.getRule(), rule);
    }

    @Test(expected = SemanticException.class)
    public void testValueRefersToNonexistentRule() throws SemanticException {
        format.addChild(new Value("nonexistent"));

        new Specification(decoder, disassembler).accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateRule() throws SemanticException {
        decoder.addChildren(new Rule("name"), new Rule("name"));

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateRuleField() throws SemanticException {
        decoder.addChildren(new Rule("name"), new Rule("NAME"));

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateSubrule() throws SemanticException {
        variant.setReturnSubrule(new Subrule("subrule"));
        Subrule subrule1 = new Subrule("subrule", 1, null);
        Subrule subrule2 = new Subrule("subrule", 2, null);
        variant.addChildren(subrule1, subrule2);

        decoder.accept(new ResolveNamesVisitor());
    }
}