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
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static net.emustudio.edigen.passes.PassUtils.mkRule;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DetectRootRulesVisitorTest {

    @Test(expected = SemanticException.class)
    public void testNonExistingRootRulesAreDetected() throws SemanticException {
        Set<String> rootRuleNames = new HashSet<>();
        rootRuleNames.add("rule");

        Decoder decoder = new Decoder(rootRuleNames);
        decoder.accept(new DetectRootRulesVisitor());
    }

    @Test
    public void testRootRulesAreDetected() throws SemanticException {
        Set<String> rootRuleNames = new HashSet<>();
        rootRuleNames.add("a");
        rootRuleNames.add("b");

        Rule a = mkRule("a");
        Rule b = mkRule("b");
        Rule c = mkRule("c");

        Decoder decoder = new Decoder(rootRuleNames);
        decoder.addChildren(a, b, c);

        decoder.accept(new DetectRootRulesVisitor());
        assertTrue(a.isRoot());
        assertTrue(b.isRoot());
        assertFalse(c.isRoot());
    }

    @Test(expected = SemanticException.class)
    public void testMultipleDeclarationsOfRootRuleIsProhibited() throws SemanticException {
        Set<String> rootRuleNames = new HashSet<>();
        rootRuleNames.add("a");
        rootRuleNames.add("b");

        Rule a = mkRule("a", "b");
        Decoder decoder = new Decoder(rootRuleNames);
        decoder.addChild(a);

        decoder.accept(new DetectRootRulesVisitor());
    }
}
