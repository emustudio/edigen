/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.BitSequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatternTest {

    @Test
    public void testConstructor() {
        BitSequence bits = BitSequence.fromBinary("1010");
        Pattern pattern = new Pattern(bits);
        
        assertEquals(bits, pattern.getBits());
    }

    @Test
    public void testGetBits() {
        BitSequence bits = BitSequence.fromBinary("11110000");
        Pattern pattern = new Pattern(bits);
        
        assertSame(bits, pattern.getBits());
    }

    @Test
    public void testAndWithMask() {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1111"));
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        
        Pattern result = pattern.and(mask);
        
        assertEquals(BitSequence.fromBinary("1010"), result.getBits());
    }

    @Test
    public void testToString() {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1010"));
        String str = pattern.toString();
        
        assertTrue(str.contains("Pattern:"));
        assertTrue(str.contains("1010"));
    }

    @Test
    public void testEquals() {
        Pattern pattern1 = new Pattern(BitSequence.fromBinary("1010"));
        Pattern pattern2 = new Pattern(BitSequence.fromBinary("1010"));
        
        assertEquals(pattern1, pattern2);
    }

    @Test
    public void testEqualsSameObject() {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1010"));
        assertEquals(pattern, pattern);
    }

    @Test
    public void testNotEqualsNull() {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1010"));
        assertNotEquals(null, pattern);
    }

    @Test
    public void testNotEqualsDifferentClass() {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1010"));
        assertNotEquals(pattern, "string");
    }

    @Test
    public void testNotEqualsDifferentBits() {
        Pattern pattern1 = new Pattern(BitSequence.fromBinary("1010"));
        Pattern pattern2 = new Pattern(BitSequence.fromBinary("1111"));
        
        assertNotEquals(pattern1, pattern2);
    }

    @Test
    public void testHashCode() {
        Pattern pattern1 = new Pattern(BitSequence.fromBinary("1010"));
        Pattern pattern2 = new Pattern(BitSequence.fromBinary("1010"));
        
        assertEquals(pattern1.hashCode(), pattern2.hashCode());
    }

    @Test
    public void testShallowCopy() {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1010"));
        
        Pattern copy = (Pattern) pattern.shallowCopy();
        
        assertNotSame(pattern, copy);
        assertEquals(pattern.getBits(), copy.getBits());
    }

    @Test
    public void testAccept() throws SemanticException {
        Pattern pattern = new Pattern(BitSequence.fromBinary("1010"));
        final boolean[] visited = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Pattern p) {
                visited[0] = true;
            }
        };
        
        pattern.accept(visitor);
        assertTrue(visited[0]);
    }

    @Test
    public void testAndPreservesOriginal() {
        Pattern original = new Pattern(BitSequence.fromBinary("1111"));
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        
        Pattern result = original.and(mask);
        
        assertEquals(BitSequence.fromBinary("1111"), original.getBits());
        assertEquals(BitSequence.fromBinary("1010"), result.getBits());
    }
}
