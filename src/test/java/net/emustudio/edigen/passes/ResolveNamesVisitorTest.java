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
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.nodes.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
        // rule = subrule: subrule(1);

        variant.setReturnSubrule(new Subrule("subrule"));
        Subrule subrule = new Subrule("subrule", 1, null);
        variant.addChild(subrule);

        decoder.accept(new ResolveNamesVisitor());
        assertEquals(variant.getReturnSubrule(), subrule);
    }

    @Test(expected = SemanticException.class)
    public void testVariantReturnsNonexistentSubrule() throws SemanticException {
        // rule = subrule: ;
        variant.setReturnSubrule(new Subrule("subrule"));

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test
    public void testSubruleHasAssociatedRule() throws SemanticException {
        // rule = rule2 ;
        // rule2 = ;
        Subrule subrule = new Subrule("rule2");
        variant.addChild(subrule);
        Rule rule2 = new Rule("rule2");
        decoder.addChild(rule2);

        decoder.accept(new ResolveNamesVisitor());
        assertEquals(subrule.getRule(), rule2);
    }

    @Test(expected = SemanticException.class)
    public void testSubruleRefersToNonexistentRule() throws SemanticException {
        // rule = nonexistent ;
        variant.addChild(new Subrule("nonexistent"));

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test
    public void testSubruleRefersToNonexistentRuleWithLength() throws SemanticException {
        // rule = nonexistent(10);
        Subrule subrule = new Subrule("nonexistent", 10, null);
        variant.addChild(subrule);

        decoder.accept(new ResolveNamesVisitor());
        assertNotNull(subrule.getRule());

        // inferred variant should return inferred subrule
        Variant inferredVariant = (Variant) subrule.getRule().getChild(0);
        assertEquals(inferredVariant.getChild(0), inferredVariant.getReturnSubrule());

        // inferred subrule should not have set a rule
        assertNull(inferredVariant.getReturnSubrule().getRule());
    }

    @Test
    public void testValueHasAssociatedRule() throws SemanticException {
        // "" = rule ;
        Value value = new Value("rule");
        format.addChild(value);

        new Specification(decoder, disassembler).accept(new ResolveNamesVisitor());
        assertEquals(value.getRule(), rule);
    }

    @Test(expected = SemanticException.class)
    public void testValueRefersToNonexistentRule() throws SemanticException {
        // "" = nonexistant ;
        format.addChild(new Value("nonexistent"));

        new Specification(decoder, disassembler).accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateRule() throws SemanticException {
        // name = ;
        // name = ;
        decoder.addChildren(new Rule("name"), new Rule("name"));
        decoder.accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateRuleCaseSensitive() throws SemanticException {
        // name = ;
        // NAME = ;
        decoder.addChildren(new Rule("name"), new Rule("NAME"));
        decoder.accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateSubrule() throws SemanticException {
        // rule = subrule: subrule(1) subrule(2);
        variant.setReturnSubrule(new Subrule("subrule"));
        Subrule subrule1 = new Subrule("subrule", 1, null);
        Subrule subrule2 = new Subrule("subrule", 2, null);
        variant.addChildren(subrule1, subrule2);

        decoder.accept(new ResolveNamesVisitor());
    }

    @Test(expected = SemanticException.class)
    public void testDuplicateRuleAlternativeName() throws SemanticException {
        // instruction, src = ;
        // data, src = ;
        decoder.addChildren(new Rule(List.of("instruction", "src")), new Rule(List.of("data", "src")));
        decoder.accept(new ResolveNamesVisitor());
    }
}