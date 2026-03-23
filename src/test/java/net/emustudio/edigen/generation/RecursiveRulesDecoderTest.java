/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;
import net.emustudio.emulib.plugins.cpu.Decoder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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

        assertEquals(1, noPrefix.image.length);
        assertEquals(2, onePrefix.image.length);
        assertEquals(3, twoPrefixes.image.length);
        assertArrayEquals(new byte[]{(byte) 0xEF}, noPrefix.image);
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xEF}, onePrefix.image);
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xEF}, twoPrefixes.image);
    }

    @Test
    public void testRecursiveInstructionIsCachedUsingItsActualLength() throws Exception {
        memory.writeBytes(0x100, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xEF});

        DecodedInstruction first = decoder.decode(0x100);
        DecodedInstruction second = decoder.decode(0x100);

        assertSame(first, second);
        assertEquals(4, memory.readCount);
    }
}
