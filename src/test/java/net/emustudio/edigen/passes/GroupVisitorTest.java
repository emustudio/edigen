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

public class GroupVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }

    @Test
    public void testMasksAndPatternsAreGrouped() throws SemanticException {
        Rule rule = (Rule) mkRule("rule").addChildren(
                nest(
                        mkMask("111"),
                        mkPattern("110"),
                        mkVariant()
                ), nest(
                        mkMask("111"),
                        mkPattern("111"),
                        mkVariant()
                )
        );

        decoder.addChild(rule);
        decoder.accept(new GroupVisitor());

        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkMask("111").addChildren(
                        nest(
                                mkPattern("110"),
                                mkVariant()
                        ), nest(
                                mkPattern("111"),
                                mkVariant()
                        )
                )
        ));
    }

    @Test
    public void testTwoRulesWithSameMasksAreKeptSeparate() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder().addChildren(
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                ),
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                )
        );

        decoder.accept(new GroupVisitor());

        assertTreesAreEqual(decoder, new Decoder().addChildren(
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                ),
                mkRule("rule").addChildren(
                        nest(
                                mkMask("111"),
                                mkPattern("110"),
                                mkVariant()
                        )
                )
        ));
    }
}
