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
import org.junit.Test;

import static net.emustudio.edigen.passes.PassUtils.*;

public class DetectUnusedRulesVisitorTest {

    @Test(expected = SemanticException.class)
    public void testUnusedRuleIsDetected() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder().addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used", 7, null)
                ), nest(
                        mkRule("unused"),
                        mkVariant("haha"),
                        mkSubrule("unused", 5, null)
                )
        );

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new DetectUnusedRulesVisitor());
    }

    @Test
    public void testRuleUsedLaterIsNotDetectedAsUnused() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder().addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used2", 7, null)
                ), nest(
                        mkRule("used2"),
                        mkVariant("haha"),
                        mkSubrule("unused", 5, null)
                )
        );
        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new DetectUnusedRulesVisitor());
    }

    @Test
    public void testRootRulesAreNotTreatedAsUnused() throws SemanticException {
        Decoder decoder = (Decoder) new Decoder("rule", "unused").addChildren(
                nest(
                        mkRule("rule"),
                        mkVariant("x"),
                        mkSubrule("used")
                ), nest(
                        mkRule("used"),
                        mkVariant("y"),
                        mkSubrule("used", 7, null)
                ), nest(
                        mkRule("unused"),
                        mkVariant("haha"),
                        mkSubrule("unused", 5, null)
                )
        );

        decoder.accept(new ResolveNamesVisitor());
        decoder.accept(new DetectUnusedRulesVisitor());
    }
}
