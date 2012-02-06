/*
 * Copyright (C) 2012 Matúš Sulír
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
package edigen.objects;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of the BitSequence class.
 * @author Matúš Sulír
 */
public class BitSequenceTest {

    /**
     * Test of fromBinary method, of class BitSequence.
     */
    @Test
    public void testFromBinary() {
        String binaryString = "01100";
        boolean[] expected = {false, true, true, false, false};
        boolean[] result = BitSequence.fromBinary(binaryString).toBooleanArray();
        
        assertTrue(Arrays.equals(expected, result));
    }

    /**
     * Test of fromHexadecimal method, of class BitSequence.
     */
    @Test
    public void testFromHexadecimal() {
        String hexString = "7bC";
        boolean[] expected = {
            false, true, true, true,
            true, false, true, true,
            true, true, false, false
        };
        boolean[] result = BitSequence.fromHexadecimal(hexString).toBooleanArray();
        
        assertTrue(Arrays.equals(expected, result));
    }

    /**
     * Test of get method, of class BitSequence.
     */
    @Test
    public void testGet() {
        BitSequence bits = new BitSequence(2);
        bits.set(1, true);
        
        assertEquals(false, bits.get(0));
        assertEquals(true, bits.get(1));
        
        try {
            bits.get(2);
            fail("The expected exception was not thrown.");
        } catch (IndexOutOfBoundsException ex) { }
    }
    
    /**
     * Test of set method, of class BitSequence.
     */
    @Test
    public void testSet() {
        BitSequence bits = new BitSequence();
        
        bits.set(0, true);
        bits.set(1, false);
        bits.set(3, true);
        bits.set(4, false);
        
        assertEquals(true, bits.get(0));
        assertEquals(false, bits.get(1));
        assertEquals(false, bits.get(2));
        assertEquals(true, bits.get(3));
        assertEquals(false, bits.get(4));
        
        assertEquals(5, bits.getLength());
    }

    /**
     * Test of append method, of class BitSequence.
     */
    @Test
    public void testAppend() {
        BitSequence bits = new BitSequence();
        bits.append(new BitSequence(2, true));
        boolean[] expected = {true, true};
        boolean[] result = bits.toBooleanArray();
        assertTrue(Arrays.equals(expected, result));
        
        BitSequence appended = new BitSequence(3);
        appended.set(0, true);
        bits.append(appended);
        expected = new boolean[] {true, true, true, false, false};
        result = bits.toBooleanArray();
        assertTrue(Arrays.equals(expected, result));
    }
    
    /**
     * Test of split method, of class BitSequence.
     */
    @Test
    public void testSplit() {
        BitSequence bits = new BitSequence(2 * 16 + 3);
        bits.set(3, true);
        bits.set(2 * 16 + 1, true);
        BitSequence[] result = bits.split(2);
        
        BitSequence[] expected = {
            new BitSequence(16),
            new BitSequence(16),
            new BitSequence(3)
        };
        
        expected[0].set(3, true);
        expected[2].set(1, true);
        
        assertTrue(Arrays.equals(expected, result));
    }
}
