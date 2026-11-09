/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugin.cpu.Decoder;
import net.emustudio.emulib.plugin.cpu.DecodedInstruction;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/** Tests captured instruction facts against mutable memory and the decoder's cache. */
public class CapturedInstructionTest {
    private static final String SPEC =
            "root instruction;\n"
                    + "instruction = \"nop\": 0x00 | \"halt\": 0xFF | \"load\": 0x01 immediate;\n"
                    + "immediate = immediate: immediate(8);\n"
                    + "%%\n"
                    + "\"%s\" = instruction;\n"
                    + "\"%s %d\" = instruction immediate;\n";

    private static EdigenTestCompiler.Compiled compiled;
    private FakeByteMemory memory;
    private Decoder decoder;

    @BeforeClass
    public static void compileOnce() throws Exception {
        compiled = EdigenTestCompiler.compile(SPEC);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        compiled.close();
    }

    @Before
    public void setUp() {
        memory = new FakeByteMemory();
        decoder = compiled.newDecoder(memory);
    }

    @Test
    public void capturedAccessorsDoNotReadMemoryAgain() throws Exception {
        DecodedInstruction instruction = decoder.decode(0);
        int reads = memory.readCount;

        assertEquals("nop", instruction.mnemonic());
        assertEquals("00", instruction.encoding());
        assertEquals(1, instruction.length());

        assertEquals(reads, memory.readCount);
        assertSame(instruction, decoder.decode(0));
    }

    @Test
    public void sameAddressOpcodeChangeUpdatesMnemonicAndBytes() throws Exception {
        assertEquals("nop", decoder.decode(0).mnemonic());
        memory.writeBytes(0, new byte[]{(byte) 0xFF});
        DecodedInstruction changed = decoder.decode(0);
        assertEquals("halt", changed.mnemonic());
        assertEquals("FF", changed.encoding());
    }

    @Test
    public void sameAddressOperandChangeUpdatesMnemonicAndBytes() throws Exception {
        memory.writeBytes(0, new byte[]{1, 7});
        assertEquals("load 7", decoder.decode(0).mnemonic());
        memory.writeBytes(1, new byte[]{9});
        DecodedInstruction changed = decoder.decode(0);
        assertEquals("load 9", changed.mnemonic());
        assertEquals("01 09", changed.encoding());
    }

    @Test
    public void decodingAfterLengthChangeCapturesConsistentExtentAndText() throws Exception {
        assertEquals("nop", decoder.decode(0).mnemonic());
        memory.writeBytes(0, new byte[]{1, 7});
        DecodedInstruction longer = decoder.decode(0);
        assertEquals(2, longer.length());
        assertEquals("load 7", longer.mnemonic());
        memory.writeBytes(0, new byte[]{0});
        DecodedInstruction shorter = decoder.decode(0);
        assertEquals(1, shorter.length());
        assertEquals("nop", shorter.mnemonic());
    }

    @Test
    public void capturedInstructionSurvivesMemoryChanges() throws Exception {
        DecodedInstruction before = decoder.decode(0);
        memory.writeBytes(0, new byte[]{1, 3});
        assertEquals("load 3", decoder.decode(0).mnemonic());
        assertEquals(1, before.length());
        assertEquals("nop", before.mnemonic());
        assertEquals("00", before.encoding());
    }
}
