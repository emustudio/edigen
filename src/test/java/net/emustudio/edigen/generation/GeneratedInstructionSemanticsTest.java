/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;
import net.emustudio.emulib.plugin.cpu.Decoder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratedInstructionSemanticsTest {

    private static final String SPEC =
            "root instruction;\n"
                    + "instruction =\n"
                    + "    \"load\": 0x01 imm8 |\n"
                    + "    \"shift\": 0x02 shift8 |\n"
                    + "    \"wide\": 0x03 imm16 |\n"
                    + "    \"op\": 1111 imm32(32) 0000;\n"
                    + "imm8 = imm8: imm8(8);\n"
                    + "shift8 = shift8: shift8(8);\n"
                    + "imm16 = imm16: imm16(16);\n"
                    + "imm32 = imm32: imm32(32);\n"
                    + "%%\n"
                    + "\"%s %X\" = instruction imm8;\n"
                    + "\"%s %X\" = instruction shift8(shift_left);\n"
                    + "\"%s %X\" = instruction imm16(reverse_bytes);\n"
                    + "\"%s %X\" = instruction imm32;\n";

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
    public void testGeneratedDecoderCapturesStringAndBitSemantics() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x01, (byte) 0xAB});

        DecodedInstruction instruction = decoder.decode(0x100);
        assertEquals(0x100, instruction.address());
        assertEquals(2, instruction.length());
        assertEquals("01 AB", instruction.encoding());
        assertEquals("load AB", instruction.mnemonic());
    }

    @Test
    public void testKeyWithStringAndBitsSelectsOneRenderingParameter() throws Exception {
        String spec = "root instruction;\n"
                + "instruction = \"prefix\": 0xFF instruction | operand: operand[0x80](8);\n"
                + "%%\n"
                + "\"%X\" = instruction;\n";

        try (EdigenTestCompiler.Compiled mixed = EdigenTestCompiler.compile(spec)) {
            Decoder mixedDecoder = mixed.newDecoder(memory);
            memory.writeBytes(0x100, new byte[]{(byte) 0xFF, (byte) 0x80});

            DecodedInstruction instruction = mixedDecoder.decode(0x100);

            assertEquals(2, instruction.length());
            assertEquals("80", instruction.mnemonic());
            assertEquals("FF 80", instruction.encoding());
        }
    }

    @Test
    public void testGeneratedRenderingAppliesStrategies() throws Exception {
        memory.writeBytes(0x200, new byte[]{0x02, (byte) 0x81});

        assertEquals("shift 2", decoder.decode(0x200).mnemonic());
    }

    @Test
    public void testDecoderReadsUnalignedThirtyTwoBitOperandAcrossFiveBytes() throws Exception {
        byte[] image = {(byte) 0xF1, 0x23, 0x45, 0x67, (byte) 0x80};
        memory.writeBytes(0x300, image);

        DecodedInstruction instruction = decoder.decode(0x300);
        assertEquals(5, instruction.length());
        assertEquals("F1 23 45 67 80", instruction.encoding());
        assertEquals("op 12345678", instruction.mnemonic());
    }

    @Test
    public void testRepeatedRenderingDoesNotMutateCapturedOperand() throws Exception {
        memory.writeBytes(0x220, new byte[]{0x03, 0x00, (byte) 0xFF});

        DecodedInstruction instruction = decoder.decode(0x220);
        assertEquals("03 00 FF", instruction.encoding());
        assertEquals("wide FF00", instruction.mnemonic());
        assertEquals("wide FF00", decoder.decode(0x220).mnemonic());
        assertEquals("wide FF00", decoder.decode(0x220).mnemonic());
        assertEquals("03 00 FF", instruction.encoding());
    }
}
