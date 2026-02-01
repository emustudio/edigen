/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.BitSequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class MaskTest {

    @Test
    public void testConstructor() {
        BitSequence bits = BitSequence.fromBinary("1010");
        Mask mask = new Mask(bits);
        
        assertEquals(bits, mask.getBits());
    }

    @Test
    public void testGetBits() {
        BitSequence bits = BitSequence.fromBinary("11110000");
        Mask mask = new Mask(bits);
        
        assertSame(bits, mask.getBits());
    }

    @Test
    public void testGetStartDefault() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        assertNull(mask.getStart());
    }

    @Test
    public void testSetAndGetStart() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        mask.setStart(42);
        
        assertEquals(Integer.valueOf(42), mask.getStart());
    }

    @Test
    public void testAnd() {
        Mask mask1 = new Mask(BitSequence.fromBinary("1111"));
        Mask mask2 = new Mask(BitSequence.fromBinary("1010"));
        
        Mask result = mask1.and(mask2);
        
        assertEquals(BitSequence.fromBinary("1010"), result.getBits());
    }

    @Test
    public void testToString() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        String str = mask.toString();
        
        assertTrue(str.contains("Mask:"));
        assertTrue(str.contains("1010"));
    }

    @Test
    public void testToStringWithStart() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        mask.setStart(5);
        
        String str = mask.toString();
        
        assertTrue(str.contains("Mask:"));
        assertTrue(str.contains("1010"));
        assertTrue(str.contains("start: 5"));
    }

    @Test
    public void testEquals() {
        Mask mask1 = new Mask(BitSequence.fromBinary("1010"));
        Mask mask2 = new Mask(BitSequence.fromBinary("1010"));
        
        assertEquals(mask1, mask2);
    }

    @Test
    public void testEqualsSameObject() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        assertEquals(mask, mask);
    }

    @Test
    public void testNotEqualsNull() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        assertNotEquals(null, mask);
    }

    @Test
    public void testNotEqualsDifferentClass() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        assertNotEquals(mask, "string");
    }

    @Test
    public void testNotEqualsDifferentBits() {
        Mask mask1 = new Mask(BitSequence.fromBinary("1010"));
        Mask mask2 = new Mask(BitSequence.fromBinary("1111"));
        
        assertNotEquals(mask1, mask2);
    }

    @Test
    public void testEqualsWithSameStart() {
        Mask mask1 = new Mask(BitSequence.fromBinary("1010"));
        Mask mask2 = new Mask(BitSequence.fromBinary("1010"));
        mask1.setStart(5);
        mask2.setStart(5);
        
        assertEquals(mask1, mask2);
    }

    @Test
    public void testNotEqualsWithDifferentStart() {
        Mask mask1 = new Mask(BitSequence.fromBinary("1010"));
        Mask mask2 = new Mask(BitSequence.fromBinary("1010"));
        mask1.setStart(5);
        mask2.setStart(10);
        
        assertNotEquals(mask1, mask2);
    }

    @Test
    public void testHashCode() {
        Mask mask1 = new Mask(BitSequence.fromBinary("1010"));
        Mask mask2 = new Mask(BitSequence.fromBinary("1010"));
        
        assertEquals(mask1.hashCode(), mask2.hashCode());
    }

    @Test
    public void testShallowCopy() {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        mask.setStart(5);
        
        Mask copy = (Mask) mask.shallowCopy();
        
        assertNotSame(mask, copy);
        assertEquals(mask.getBits(), copy.getBits());
        assertEquals(mask.getStart(), copy.getStart());
    }

    @Test
    public void testAccept() throws SemanticException {
        Mask mask = new Mask(BitSequence.fromBinary("1010"));
        final boolean[] visited = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Mask m) {
                visited[0] = true;
            }
        };
        
        mask.accept(visitor);
        assertTrue(visited[0]);
    }
}
