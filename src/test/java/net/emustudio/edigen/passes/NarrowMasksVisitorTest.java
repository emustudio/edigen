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
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import org.junit.Before;
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class NarrowMasksVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        this.decoder = new Decoder();
    }


    @Test
    public void testMasksAreNarrowed() throws SemanticException {
        Rule rule = (Rule) mkRule("rule").addChildren(
                mkMask("110").addChild(mkPattern("110")),
                mkMask("010").addChild(mkPattern("010")),
                mkMask("1111").addChild(mkPattern("1110")),
                mkMask("0010").addChild(mkPattern("0010"))
        );

        decoder.addChild(rule);
        decoder.accept(new NarrowMasksVisitor());

        assertTreesAreEqual(rule, nest(
                mkRule("rule"),
                mkMask("110").addChildren(
                        mkPattern("110"), nest(
                                mkPattern(""),
                                mkMask("010").addChildren(
                                        mkPattern("010"),
                                        nest(
                                                mkPattern(""),
                                                mkMask("1111").addChildren(
                                                        mkPattern("1110"),
                                                        nest(
                                                                mkPattern(""),
                                                                mkMask("0010"),
                                                                mkPattern("0010")
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));
    }
}
