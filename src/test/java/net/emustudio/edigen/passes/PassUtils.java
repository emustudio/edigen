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

import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.*;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PassUtils {

    public static String mkString(int length, char c) {
        char[] data = new char[length];
        Arrays.fill(data, c);
        return String.valueOf(data);
    }

    public static Rule mkRule(String name) {
        return new Rule(name);
    }

    public static Rule mkRule(String... names) {
        return new Rule(Arrays.asList(names));
    }

    public static Variant mkVariant() {
        return new Variant();
    }

    public static Variant mkVariant(String returnString) {
        Variant v = new Variant();
        v.setReturnString(returnString);
        return v;
    }

    public static Variant mkVariant(Subrule returnSubrule) {
        Variant v = new Variant();
        v.setReturnSubrule(returnSubrule);
        return v;
    }

    public static Subrule mkSubrule(String name) {
        return new Subrule(name);
    }

    public static Subrule mkSubrule(String name, Integer length, Pattern prePattern) {
        return new Subrule(name, length, prePattern);
    }

    public static Subrule mkSubrule(String name, Integer length, Pattern prePattern, int start, Rule rule) {
        Subrule s = new Subrule(name, length, prePattern);
        s.setStart(start);
        s.setRule(rule);
        return s;
    }

    public static Mask mkMask(String bits) {
        return new Mask(BitSequence.fromBinary(bits));
    }

    public static Mask mkMask(String bits, int start) {
        Mask mask = new Mask(BitSequence.fromBinary(bits));
        mask.setStart(start);
        return mask;
    }

    public static Pattern mkPattern(String bits) {
        return new Pattern(BitSequence.fromBinary(bits));
    }

    public static <T extends TreeNode> T nest(T fst, TreeNode... nodes) {
        TreeNode parent = fst;
        for (TreeNode node : nodes) {
            parent.addChild(node);
            parent = node;
        }
        return fst;
    }

    public static void assertTreesAreIsomorphic(TreeNode fst, TreeNode snd) {
        assertEquals(fst.childCount(), snd.childCount());
        for (int i = 0; i < fst.childCount(); i++) {
            TreeNode fstChild = fst.getChild(i);
            TreeNode sndChild = snd.getChild(i);
            assertEquals(fstChild.getClass(), sndChild.getClass());
            assertTreesAreIsomorphic(fstChild, sndChild);
        }
    }

    public static void assertTreesAreEqual(TreeNode fst, TreeNode snd) {
        assertEquals(fst.childCount(), snd.childCount());
        for (int i = 0; i < fst.childCount(); i++) {
            TreeNode fstChild = fst.getChild(i);
            TreeNode sndChild = snd.getChild(i);
            assertEquals(fstChild, sndChild);
            assertTreesAreEqual(fstChild, sndChild);
        }
    }
}
