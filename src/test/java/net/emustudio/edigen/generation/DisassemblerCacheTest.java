/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.cpu.Decoder;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/** Tests generated disassembly against mutable memory and the decoder's cache. */
public class DisassemblerCacheTest {
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
    private Disassembler disassembler;

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
        disassembler = compiled.newDisassembler(memory, decoder);
    }

    @Test
    public void repeatedDisassemblyReusesUnchangedDecodedInstruction() throws Exception {
        Object instruction = decoder.decode(0);
        assertEquals("nop", disassembler.disassemble(0).mnemo);
        assertEquals("nop", disassembler.disassemble(0).mnemo);
        assertEquals(1, disassembler.getNextInstructionPosition(0));
        assertSame(instruction, decoder.decode(0));
    }

    @Test
    public void sameAddressOpcodeChangeUpdatesMnemonicAndBytes() throws Exception {
        assertEquals("nop", disassembler.disassemble(0).mnemo);
        memory.writeBytes(0, new byte[]{(byte) 0xFF});
        assertEquals("halt", disassembler.disassemble(0).mnemo);
        assertEquals("FF", disassembler.disassemble(0).opCode);
    }

    @Test
    public void sameAddressOperandChangeUpdatesMnemonicAndBytes() throws Exception {
        memory.writeBytes(0, new byte[]{1, 7});
        assertEquals("load 7", disassembler.disassemble(0).mnemo);
        memory.writeBytes(1, new byte[]{9});
        assertEquals("load 9", disassembler.disassemble(0).mnemo);
        assertEquals("01 09", disassembler.disassemble(0).opCode);
    }

    @Test
    public void lengthChangeBetweenDisassemblyAndNavigationUsesCurrentMemory() throws Exception {
        assertEquals("nop", disassembler.disassemble(0).mnemo);
        memory.writeBytes(0, new byte[]{1, 7});
        assertEquals(2, disassembler.getNextInstructionPosition(0));
        assertEquals("load 7", disassembler.disassemble(0).mnemo);
        memory.writeBytes(0, new byte[]{0});
        assertEquals(1, disassembler.getNextInstructionPosition(0));
        assertEquals("nop", disassembler.disassemble(0).mnemo);
    }

    @Test
    public void navigationThenDisassemblyRevalidatesSameAddress() throws Exception {
        assertEquals(1, disassembler.getNextInstructionPosition(0));
        memory.writeBytes(0, new byte[]{1, 3});
        assertEquals("load 3", disassembler.disassemble(0).mnemo);
    }
}
