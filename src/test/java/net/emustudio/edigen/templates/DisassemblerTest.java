package net.emustudio.edigen.templates;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DisassemblerTest {

    @Test
    public void testAbsoluteStrategyWorks() {
        assertEquals(0x1D, absolute(new Byte[] { 0x1D })[0].byteValue());
        assertEquals(127, absolute(new Byte[] { (byte)0x81 })[0].byteValue());
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
}
