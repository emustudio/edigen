/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;
import net.emustudio.emulib.plugins.cpu.Decoder;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.runtime.helpers.Bits;
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
                    + "    \"op\": 1111 imm32(32) 0000;\n"
                    + "imm8 = imm8: imm8(8);\n"
                    + "shift8 = shift8: shift8(8);\n"
                    + "imm32 = imm32: imm32(32);\n"
                    + "%%\n"
                    + "\"%s %X\" = instruction imm8;\n"
                    + "\"%s %X\" = instruction shift8(shift_left);\n"
                    + "\"%s %X\" = instruction imm32;\n";

    private static EdigenTestCompiler.Compiled compiled;
    private static int instructionKey;
    private static int imm8Key;
    private static int shift8Key;
    private static int imm32Key;

    private FakeByteMemory memory;
    private Decoder decoder;
    private Disassembler disassembler;

    @BeforeClass
    public static void compileOnce() throws Exception {
        compiled = EdigenTestCompiler.compile(SPEC);
        instructionKey = getRuleCode("INSTRUCTION");
        imm8Key = getRuleCode("IMM8");
        shift8Key = getRuleCode("SHIFT8");
        imm32Key = getRuleCode("IMM32");
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
        disassembler = compiled.newDisassembler(memory, decoder);
    }

    @Test
    public void testGeneratedDecoderPreservesStringAndBitSemantics() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x01, (byte) 0xAB});

        DecodedInstruction instruction = decoder.decode(0x100);
        Bits bits = instruction.getBits(imm8Key);

        assertTrue(instruction.hasKey(instructionKey));
        assertTrue(instruction.hasKey(imm8Key));
        assertEquals("load", instruction.getString(instructionKey));
        assertNotNull(bits);
        assertEquals(0xAB, bits.number);
        assertSame(bits, instruction.getBits(imm8Key));
        assertEquals(2, instruction.getKeys().size());
        assertTrue(instruction.getKeys().contains(instructionKey));
        assertTrue(instruction.getKeys().contains(imm8Key));
        assertEquals("load AB", disassembler.disassemble(0x100).getMnemo());
    }

    @Test
    public void testGeneratedDisassemblerAppliesStrategies() throws Exception {
        memory.writeBytes(0x200, new byte[]{0x02, (byte) 0x81});

        assertEquals("shift 2", disassembler.disassemble(0x200).getMnemo());
    }

    @Test
    public void testDecoderReadsUnalignedThirtyTwoBitOperandAcrossFiveBytes() throws Exception {
        byte[] image = {(byte) 0xF1, 0x23, 0x45, 0x67, (byte) 0x80};
        memory.writeBytes(0x300, image);

        DecodedInstruction instruction = decoder.decode(0x300);
        Bits bits = instruction.getBits(imm32Key);

        assertTrue(instruction.hasKey(instructionKey));
        assertTrue(instruction.hasKey(imm32Key));
        assertFalse(instruction.hasKey(shift8Key));
        assertNotNull(bits);
        assertEquals(0x12345678, bits.number);
        assertArrayEquals(image, instruction.getImage());
        assertEquals("op 12345678", disassembler.disassemble(0x300).getMnemo());
    }

    private static int getRuleCode(String fieldName) throws Exception {
        return compiled.getDecoderClass().getField(fieldName).getInt(null);
    }
}
