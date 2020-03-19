package net.emustudio.edigen.templates;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DisassemblerTest {

    @Test
    public void testAbsoluteStrategyWorks() {
        assertEquals(0x1D, absolute(new Byte[] { 0x1D })[0].byteValue());
        assertEquals(127, absolute(new Byte[] { (byte)0x81 })[0].byteValue());
    }

    @Test
    public void testBitReverseWorks() {
        assertEquals((byte)0b11100000, bit_reverse(new byte[] { 0b000111})[0]);
    }

    @Test
    public void testShiftLeftWorks() {
        assertArrayEquals(new byte[] { 2, 4 }, shift_left(new byte[] { 1,2 }));
    }

    @Test
    public void testShiftRightWorks() {
        assertArrayEquals(new byte[] { 0, (byte)0b10000001 }, shift_right(new byte[] { 1,2 }));
    }

    public static Byte[] absolute(Byte[] value) {
        Byte[] result = new Byte[value.length];
        if (value.length > 0) {
            if ((value[0] & 0x80) == 0x80) {
                for (int octet = 0; octet < result.length; octet++) {
                    result[octet] = (byte)(~value[octet] & 0xFF);
                }
                for (int octet = 0; octet < result.length; octet++) {
                    if ((short)result[octet] + 1 > (byte)(result[octet] + 1)) {
                        result[octet] = 0;
                    } else {
                        result[octet] = (Byte)(byte)(result[octet] + 1);
                        break;
                    }
                }
            } else {
                result = value;
            }
        }
        return result;
    }

    public static byte[] bit_reverse(byte[] value) {
        byte[] result = new byte[value.length];

        for (int octet = 0; octet < result.length; octet++) {
            for (int bit = 0; bit < 8; bit++) {
                result[octet] |= (value[octet] & (1 << bit)) >>> bit << (8 - bit - 1);
            }
        }

        return result;
    }

    public static byte[] shift_left(byte[] value) {
        byte[] result = new byte[value.length];

        for (int octet = 0; octet < result.length; octet++) {
            int shifted = (value[octet] << 1) & 0xFF;
            if (octet + 1 < result.length) {
                shifted |= value[octet + 1] >>> 7;
            }
            result[octet] = (byte)shifted;
        }
        return result;
    }

    public static byte[] shift_right(byte[] value) {
        byte[] result = new byte[value.length];

        for (int octet = result.length - 1; octet >= 0; octet--) {
            int shifted = (value[octet] >>> 1) & 0xFF;
            if (octet - 1 >= 0) {
                shifted |= ((value[octet - 1] & 1) << 7);
            }
            result[octet] = (byte)shifted;
        }
        return result;
    }
}
