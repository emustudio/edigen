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

import java.util.HashSet;
import java.util.Set;

import static net.emustudio.edigen.passes.PassUtils.*;
import static org.junit.Assert.assertEquals;

public class DetectUnreachableFormatsVisitorTest {
    private Decoder decoder;
    private Disassembler disassembler;
    private Specification specification;
    private Rule rootRule;

    @Before
    public void setUp() {
        decoder = new Decoder("A");
        rootRule = mkRule("A").setRoot(true, "A");
        decoder.addChildren(rootRule);
        Set<Rule> rootR = new HashSet<>();
        rootR.add(rootRule);
        decoder.setRootRules(rootR);

        disassembler = new Disassembler();
        specification = new Specification(decoder, disassembler);
    }

    @Test
    public void testMultipleVariants() throws SemanticException {
        Rule b = nest(mkRule("B"), mkVariant(new Subrule("B")));
        Rule d = nest(mkRule("D"), mkVariant(new Subrule("D")));
        Rule e = nest(mkRule("E"), mkVariant(new Subrule("E")));
        Rule f = nest(mkRule("F"), mkVariant(new Subrule("F")));
        Rule g = nest(mkRule("G"), mkVariant(new Subrule("G")));
        Rule i = nest(mkRule("I"), mkVariant(new Subrule("I")));
        Rule j = nest(mkRule("J"), mkVariant(new Subrule("J")));

        Rule c = nest(
                mkRule("C"),
                mkVariant("FG").addChildren(
                        mkSubrule("F").setRule(f),
                        mkSubrule("G").setRule(g)));

        Rule k = (Rule) mkRule("K").addChildren(
                nest(mkVariant("F"), mkSubrule("F").setRule(f)),
                nest(mkVariant("G"), mkSubrule("G").setRule(g)));

        Rule h = (Rule) mkRule("H").addChildren(
                nest(mkVariant("I"), mkSubrule("I").setRule(i)),
                nest(mkVariant("J"), mkSubrule("J").setRule(j)));


        rootRule.addChildren(
                mkVariant("BD").addChildren(
                        mkSubrule("B").setRule(b),
                        mkSubrule("D").setRule(d)),
                nest(mkVariant("E"), mkSubrule("E").setRule(e)),
                nest(mkVariant(), mkSubrule("C").setRule(c)),
                mkVariant("K").addChildren(
                        mkSubrule("K").setRule(k),
                        mkSubrule("H").setRule(h)));
        decoder.addChildren(
                b, c, d, e, f, g, h, i, j, k
        );

        runTest(Set.of(
                Set.of("A", "B", "D"),
                Set.of("A", "E"),
                Set.of("C", "F", "G"),
                Set.of("A", "K", "F", "H", "I"),
                Set.of("A", "K", "F", "H", "J"),
                Set.of("A", "K", "G", "H", "I"),
                Set.of("A", "K", "G", "H", "J")
        ));
    }

    @Test
    public void testUnorderedPath() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        rootRule.addChildren(
                mkVariant("A").addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C").setRule(rc)));

        decoder.addChildren(rb, rc);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("C"),
                        new Value("B")
                )
        );

        runTest(Set.of(Set.of("A", "B", "C")));
    }

    @Test(expected = SemanticException.class)
    public void testPartialPathIsNotAllowed() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        rootRule.addChildren(
                mkVariant("A").addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C").setRule(rc)));
        decoder.addChildren(rb, rc);

        disassembler.addChildren(
                new Format("").addChildren(
                        new Value("A"),
                        new Value("B")
                )
        );
        runTest(Set.of(Set.of("A", "B", "C")));
    }

    @Test
    public void testSubruleEmptyRuleIsIgnored() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        rootRule.addChildren(
                mkVariant("As").addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C")
                ));
        decoder.addChildren(rb);
        runTest(Set.of(Set.of("A", "B")));
    }

    @Test
    public void testNestedSubrule() throws SemanticException {
        Rule rb = nest(
                mkRule("B"),
                mkVariant(new Subrule("B")));

        Rule rd = nest(
                mkRule("D"),
                mkVariant("Ds"));

        Rule rc = nest(
                mkRule("C"),
                mkVariant().addChildren(
                        mkSubrule("D").setRule(rd)
                ));

        rootRule.addChildren(
                mkVariant().addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("C").setRule(rc)
                ));

        decoder.addChildren(rb, rc, rd);
        runTest(Set.of(Set.of("B", "D")));
    }

    @Test
    public void testCombiningReturningAndNonReturningVariant() throws SemanticException {
        Rule rc = nest(
                mkRule("C"),
                mkVariant("Cs"));

        Rule rd = nest(
                mkRule("D"),
                mkVariant("Ds"));

        Rule re = nest(
                mkRule("E"),
                mkVariant("Es"));

        Rule rf = nest(
                mkRule("F"),
                mkVariant("Fs"));


        Rule rb = (Rule) mkRule("C").addChildren(
                mkVariant().addChildren(
                        mkSubrule("E").setRule(re)),
                mkVariant("aa").addChildren(
                        mkSubrule("C").setRule(rc),
                        mkSubrule("D").setRule(rd)));

        rootRule.addChildren(
                mkVariant().addChildren(
                        mkSubrule("B").setRule(rb),
                        mkSubrule("F").setRule(rf)
                ));

        decoder.addChildren(rb, rc, rd, re, rf);
        runTest(Set.of(
                Set.of("E", "F"),
                Set.of("B", "C", "D", "F")
        ));
    }


    @Test
    public void testMultipleRuleNames() throws SemanticException {
        Rule rb = nest(
                mkRule("B", "B2"),
                mkVariant("Bs"),
                mkSubrule("B", 7, null));

        rootRule.addChildren(
                mkVariant("A").addChildren(
                        mkSubrule("B2").setRule(rb)));
        decoder.addChildren(rb);
        runTest(Set.of(
                Set.of("A", "B2")
        ));
    }

    @Test
    public void testSplitVariants() throws SemanticException {
        Rule rc = nest(
                mkRule("nested1"),
                mkVariant("nested1"));
        Rule rd = nest(
                mkRule("nested2"),
                mkVariant("nested2"));

        Rule rb = (Rule) mkRule("B").addChildren(
                nest(
                        mkVariant("C"),
                        mkSubrule("C").setRule(rc)),
                nest(
                        mkVariant("D"),
                        mkSubrule("D").setRule(rd)));

        rootRule.addChildren(
                nest(
                        mkVariant("B"),
                        mkSubrule("B").setRule(rb)));
        decoder.addChildren(rb);
        runTest(Set.of(
                Set.of("A", "B", "C"),
                Set.of("A", "B", "D")
        ));
    }

    @Test
    public void testOnlyVariant() throws SemanticException {
        rootRule.addChild(mkVariant("aa"));
        runTest(Set.of(Set.of("A")));
    }

    @Test
    public void testCombiningPlainVariantWithNestedOne() throws SemanticException {
        Rule rc = (Rule) mkRule("C").addChild(mkVariant(mkSubrule("C")));
        Rule rb = (Rule) mkRule("B").addChildren(
                mkVariant("b").addChild(mkSubrule("C").setRule(rc)),
                mkVariant("c"));
        rootRule.addChildren(
                mkVariant("a"),
                mkVariant().addChild(mkSubrule("B").setRule(rb)));
        runTest(Set.of(
                Set.of("A"),
                Set.of("B", "C"),
                Set.of("B")
        ));
    }

    private void runTest(Set<Set<String>> expected) throws SemanticException {
        DetectUnreachableFormatsVisitor visitor = new DetectUnreachableFormatsVisitor();
        visitor.visit(specification);
        assertEquals(expected, visitor.getReachable());
    }
}
