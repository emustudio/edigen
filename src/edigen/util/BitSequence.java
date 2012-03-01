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
package edigen.util;

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
    
    private static final int NIBBLE_LENGTH = 4;
    
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
        BitSequence bits = new BitSequence(digitCount * NIBBLE_LENGTH);
        
        for (int nibbleIndex = 0; nibbleIndex < digitCount; nibbleIndex++) {
            int nibble = Integer.parseInt(hexString.substring(nibbleIndex, nibbleIndex + 1), 16);
            
            for (int bitIndex = 0; bitIndex < NIBBLE_LENGTH; bitIndex++) {
                boolean bit = ((nibble >>> (3 - bitIndex)) & 1) != 0;
                bits.set(NIBBLE_LENGTH * nibbleIndex + bitIndex, bit);
            }
        }
        
        return bits;
    }
    
    /**
     * Returns the bit at the given index.
     * @param index the index to read, starting at zero
     * @return the bit as boolean
     * @throws IndexOutOfBoundsException when index is not in [0; length)
     */
    public boolean get(int index) {
        if (index < 0 || index >= length)
            throw new IndexOutOfBoundsException("Sequence index out of bounds");
        
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
     * Returns true if the sequence contains only bits of the specified value.
     * 
     * If the sequence length is zero, <code>true</code> is returned.
     * @param value the expected bit value
     * @return true if the bit sequence contains only given bits, false
     *         otherwise
     */
    public boolean containsOnly(boolean value) {
        for (int i = 0; i < length; i++) {
            if (get(i) != value)
                return false;
        }
        
        return true;
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
     * Appends one bit to the end of this sequence.
     * @param bit the value to be appended
     */
    public void append(boolean bit) {
        bitSet.set(length++, bit);
    }
    
    /**
     * Splits the sequence into shorter sequences of equal length.
     * 
     * If it is necessary, the last sequence in the returned array will contain
     * less than 8 * bytesPerPiece bits.
     * @param bitsPerPiece the number of bits in each sequence
     * @return the array of shorter sequences
     */
    public BitSequence[] split(int bitsPerPiece) {
        int count = (int) Math.ceil((double) length / bitsPerPiece);
        BitSequence[] sequences = new BitSequence[count];
        
        for (int piece = 0; piece < count; piece++) {
            sequences[piece] = new BitSequence();
            int start = piece * bitsPerPiece;
            int end = start + bitsPerPiece;
            
            for (int bit = start; bit < end && bit < length; bit++)
                sequences[piece].append(get(bit));
        }
        
        return sequences;
    }
    
    /**
     * Returns the subsequence of the current bit sequence.
     * @param start the index of the first bit, included
     * @param length the length of the subsequence, in bits
     * @return the subsequence
     * @throws IndexOutOfBoundsException if the resulting subsequence exceeded
     *         the sequence boundary
     */
    public BitSequence subSequence(int start, int length) {
        if (start < 0 || length < 0 || start + length > this.length)
            throw new IndexOutOfBoundsException("Sequence index out of bounds");
        
        BitSequence sequence = new BitSequence(length);
        
        for (int i = 0; i < length; i++)
            sequence.set(i, get(start + i));
        
        return sequence;
    }
    
    /**
     * Returns a bit sequence ANDed with an other bit sequence.
     * @param other the second sequence
     * @return the result, truncated to the length of the shorter input sequence
     */
    public BitSequence and(BitSequence other) {
        int resultLength = Math.min(this.length, other.length);
        BitSequence result = new BitSequence(resultLength);
        
        for (int i = 0; i < resultLength; i++) {
            if (this.get(i) && other.get(i))
                result.set(i, true);
        }
        
        return result;
    }
    
    /**
     * Checks whether the other bit sequence has exactly the same content and
     * length as this sequence.
     * @param object the sequence to compare
     * @return true if the sequences are same, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BitSequence))
            return false;
        
        BitSequence bits = (BitSequence) object;
        
        return (bits.length == this.length) && bits.bitSet.equals(this.bitSet);
    }

    /**
     * Returns the hash code of this sequence.
     * 
     * The algorithm was automatically generated by NetBeans IDE.
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        
        hash = 29 * hash + this.length;
        hash = 29 * hash + (this.bitSet != null ? this.bitSet.hashCode() : 0);
        
        return hash;
    }
    
    /**
     * Returns a string representation of the object.
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++)
            result.append(get(i) ? '1' : '0');
        
        return result.toString();
    }
    
    /**
     * Returns this sequence as an array of boolean values.
     * @return the boolean array
     */
    public boolean[] toBooleanArray() {
        boolean[] booleanArray = new boolean[length];
        
        for (int i = 0; i < length; i++)
            booleanArray[i] = get(i);
        
        return booleanArray;
    }
    
    /**
     * Returns the hexadecimal representation of this sequence.
     * 
     * Before computation, the sequence is zero-padded to whole nibbles from
     * the beginning.
     * @return the hexadecimal string, in lowercase
     */
    public String toHexadecimal() {
        int padBitCount = NIBBLE_LENGTH - (length % NIBBLE_LENGTH);
        
        if (padBitCount == NIBBLE_LENGTH)
            padBitCount = 0;
        
        BitSequence padded = new BitSequence(padBitCount);
        padded.append(this);
        
        BitSequence[] nibbles = padded.split(NIBBLE_LENGTH);
        StringBuilder result = new StringBuilder();
        
        for (BitSequence nibble : nibbles) {
            int digit = 0;
            int factor = 8;
            
            for (int i = 0; i < NIBBLE_LENGTH; i++) {
                if (nibble.get(i))
                    digit += factor;
                
                factor /= 2;
            }
            
            result.append(Integer.toHexString(digit));
        }
        
        return result.toString();
    }
}
