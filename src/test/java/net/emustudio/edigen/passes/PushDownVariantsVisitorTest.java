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

public class PushDownVariantsVisitorTest {
    private Decoder decoder;

    @Before
    public void setUp() {
        decoder = new Decoder();
    }

    @Test
    public void testVariantsAreMovedToBottom() throws SemanticException {
        Rule rule = (Rule) nest(
                mkRule("rule").addChild(nest(
                        mkVariant(),
                        mkMask("111"),
                        mkPattern("001")
                )),
                mkVariant(),
                mkMask("111"),
                mkPattern("001"),
                mkMask("111"),
                mkPattern("001")
        );

        decoder.addChild(rule);
        decoder.accept(new PushDownVariantsVisitor());

        assertTreesAreIsomorphic(rule, nest(
                mkRule("rule").addChild(nest(
                        mkMask("111"),
                        mkPattern("001"),
                        mkVariant()
                )),
                mkMask("111"),
                mkPattern("001"),
                mkMask("111"),
                mkPattern("001"),
                mkVariant()
        ));
    }

    @Test
    public void testVariantWithoutMaskIsKept() throws SemanticException {
        Rule rule = nest(
                mkRule("rule"),
                mkVariant()
        );
        decoder.addChild(rule);
        decoder.accept(new PushDownVariantsVisitor());

        assertTreesAreIsomorphic(rule, nest(
                mkRule("rule"),
                mkVariant()
        ));
    }
}
