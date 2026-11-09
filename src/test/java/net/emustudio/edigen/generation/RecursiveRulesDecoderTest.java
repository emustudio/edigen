/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;
import net.emustudio.emulib.plugins.cpu.Decoder;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class RecursiveRulesDecoderTest {

    private static final String SPEC =
            "root instruction;\n"
                    + "instruction = 0xFF prefixed | \"halt\": 0xEF;\n"
                    + "prefixed = instruction;\n"
                    + "%%\n"
                    + "\"%s\" = instruction;\n";

    private static EdigenTestCompiler.Compiled compiled;

    private FakeByteMemory memory;
    private Decoder decoder;

    @BeforeClass
    public static void compileOnce() throws Exception {
        compiled = EdigenTestCompiler.compile(SPEC);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (compiled != null) {
            compiled.close();
        }
    }

    @Before
    public void setUp() {
        memory = new FakeByteMemory();
        decoder = compiled.newDecoder(memory);
    }

    @Test
    public void testRecursivePrefixesIncreaseInstructionLength() throws Exception {
        memory.writeBytes(0x100, new byte[]{(byte) 0xEF});
        memory.writeBytes(0x200, new byte[]{(byte) 0xFF, (byte) 0xEF});
        memory.writeBytes(0x300, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xEF});

        DecodedInstruction noPrefix = decoder.decode(0x100);
        DecodedInstruction onePrefix = decoder.decode(0x200);
        DecodedInstruction twoPrefixes = decoder.decode(0x300);

        assertEquals(1, noPrefix.length());
        assertEquals(2, onePrefix.length());
        assertEquals(3, twoPrefixes.length());
        assertEquals("EF", noPrefix.encoding());
        assertEquals("FF EF", onePrefix.encoding());
        assertEquals("FF FF EF", twoPrefixes.encoding());
    }

    @Test
    public void testRecursiveInstructionIsCachedUsingItsActualLength() throws Exception {
        memory.writeBytes(0x100, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xEF});

        DecodedInstruction first = decoder.decode(0x100);
        DecodedInstruction second = decoder.decode(0x100);

        assertSame(first, second);
        assertEquals(4, memory.readCount);
    }

    @Test
    public void excessivePrefixesFailWithoutOverflowAndLaterDecodeStillWorks() throws Exception {
        byte[] prefixes = new byte[1024];
        java.util.Arrays.fill(prefixes, (byte) 0xFF);
        memory.writeBytes(0, prefixes);

        assertThrows(InvalidInstructionException.class, () -> decoder.decode(0));

        memory.writeBytes(0, new byte[]{(byte) 0xEF});
        assertEquals("halt", decoder.decode(0).mnemonic());
    }
}
