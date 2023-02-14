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
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class SortVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }

    @Test
    public void testMasksAreSortedFromShortestToLongest() throws SemanticException {
        // stable sort
        Rule rule = (Rule) mkRule("rule")
                .addChildren(
                        nest(
                                mkVariant(),
                                mkMask("000011")
                        ), nest(
                                mkVariant(),
                                mkMask("00000")
                        ), nest(
                                mkVariant(),
                                mkMask("1111")
                        ), nest(
                                mkVariant(),
                                mkMask("00001")
                        ), nest(
                                mkVariant(),
                                mkMask("0000")
                        )
                );

        decoder.addChild(rule);
        decoder.accept(new SortVisitor());

        assertTreesAreEqual(rule, mkRule("rule")
                .addChildren(
                        nest(
                                mkVariant(),
                                mkMask("1111")
                        ), nest(
                                mkVariant(),
                                mkMask("0000")
                        ), nest(
                                mkVariant(),
                                mkMask("00000")
                        ), nest(
                                mkVariant(),
                                mkMask("00001")
                        ), nest(
                                mkVariant(),
                                mkMask("000011")
                        )
                ));
    }

    @Test
    public void testNoMasksNoThrow() throws SemanticException {
        Rule rule = new Rule("rule");
        Decoder decoder = new Decoder();
        decoder.addChild(rule);
        decoder.accept(new SortVisitor());
    }
}
