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

import java.util.BitSet;

/**
 * A sequence of bits with defined but modifiable length.
 * 
 * Unlike {@link BitSet}, where the length is determined by the position of the
 * highest bit set to 1, this sequence has an explicitly defined bit count, so
 * it can end with zeroes and the length is preserved. The sequence length can
 * be modified after creation. In addition, several conversion methods are
 * provided.
 * @author Matúš Sulír
 */
public class BitSequence {
    
    private int length;
    private BitSet bitSet;
    
    /**
     * Constructs a bit sequence with zero length.
     */
    public BitSequence() {
        length = 0;
        bitSet = new BitSet();
    }
    
    /**
     * Constructs a bit sequence with the specified length.
     * 
     * The bit sequence is initially filled with <code>false</code> values.
     * @param length the initial sequence length, in bits
     */
    public BitSequence(int length) {
        this.length = length;
        bitSet = new BitSet(length);
    }
    
    /**
     * Constructs a bit sequence with the specified length and fills it with
     * the specified value.
     * @param length the initial sequence length, in bits
     * @param value the value to fill the sequence with
     */
    public BitSequence(int length, boolean value) {
        this(length);
        bitSet.set(0, length, value);
    }
    
    /**
     * Returns a new instance of bit sequence with the length and content
     * obtained from the binary number represented as a string.
     * 
     * Leading zeroes are included in the resulting sequence.
     * @param binaryString the string representation of the binary number; can
     *        contain only characters '0' and '1'
     * @return the constructed bit sequence
     * @throws NumberFormatException if the input contains invalid characters
     */
    public static BitSequence fromBinary(String binaryString) {
        if (!binaryString.matches("[01]+"))
            throw new NumberFormatException("Invalid binary number.");
        
        int length = binaryString.length();
        BitSequence bits = new BitSequence(length);
        
        for (int i = 0; i < length; i++) {
            if (binaryString.charAt(i) == '1')
                bits.set(i, true);
        }
        
        return bits;
    }
    
    /**
     * Returns a new instance of bit sequence with the length and content
     * obtained from the hexadecimal number represented as a string.
     * 
     * Leading zeroes are included in the resulting sequence.
     * @param hexString the string representation of the hexedecimal number; can
     *        contain only characters '0' - '9', 'a' - 'f' (or uppercase)
     * @return the constructed bit sequence
     * @throws NumberFormatException if the input contains invalid characters
     */
    public static BitSequence fromHexadecimal(String hexString) {
        if (!hexString.matches("[0-9a-fA-F]+"))
            throw new NumberFormatException("Invalid hexadecimal number.");
        
        int digitCount = hexString.length();
        BitSequence bits = new BitSequence(digitCount * 4);
        
        for (int nibbleIndex = 0; nibbleIndex < digitCount; nibbleIndex++) {
            int nibble = Integer.parseInt(hexString.substring(nibbleIndex, nibbleIndex + 1), 16);
            
            for (int bitIndex = 0; bitIndex < 4; bitIndex++) {
                boolean bit = ((nibble >>> (3 - bitIndex)) & 1) != 0;
                bits.set(4 * nibbleIndex + bitIndex, bit);
            }
        }
        
        return bits;
    }
    
    /**
     * Returns the bit at the given index.
     * @param index the index to read, starting at zero
     * @return the bit as boolean
     */
    public boolean get(int index) {
        return bitSet.get(index);
    }
    
    /**
     * Sets the bit at the given index to the specified value.
     * @param index the index to modify
     * @param value the boolean value
     */
    public void set(int index, boolean value) {
        bitSet.set(index, value);
        
        if (index >= length)
            length = index + 1;
    }
    
    /**
     * Returns the total length of this bit sequence.
     * @return the length, in bits
     */
    public int getLength() {
        return length;
    }
    
    /**
     * Appends another bit sequence to the end of this sequence.
     * @param bits the sequence to be appended
     */
    public void append(BitSequence bits) {
        int appendedLength = bits.getLength();
        
        for (int i = 0; i < appendedLength; i++)
            bitSet.set(length + i, bits.get(i));
        
        length += appendedLength;
    }
    
    /**
     * Returns this sequence as an array of boolean values.
     * @return the boolean array
     */
    public boolean[] toBooleanArray() {
        boolean[] booleanArray = new boolean[length];
        
        for (int i = 0; i < length; i++)
            booleanArray[i] = bitSet.get(i);
        
        return booleanArray;
    }
    
}
