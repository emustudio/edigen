/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;
import net.emustudio.emulib.plugins.cpu.Decoder;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the last-position cache in the <em>real</em> generated
 * disassembler produced from {@code Disassembler.edt}.
 * <p>
 * The generated disassembler's {@code cachedDecode()} avoids
 * redundant {@code decoder.decode()} calls when
 * {@link Disassembler#disassemble(int)} and
 * {@link Disassembler#getNextInstructionPosition(int)} are called
 * for the same memory address.
 * <p>
 * To count actual decode() invocations we inject a
 * {@link CountingDecoder} wrapper around the real generated decoder.
 */
public class DisassemblerCacheTest {

    /**
     * Minimal Edigen specification: two 1-byte instructions.
     */
    private static final String SPEC =
            "root instruction;\n"
                    + "instruction = \"nop\": 0x00 | \"halt\": 0xFF;\n"
                    + "%%\n"
                    + "\"%s\" = instruction;\n";

    /**
     * A transparent {@link Decoder} wrapper that counts
     * {@code decode()} calls and delegates to the real decoder.
     */
    private static class CountingDecoder implements Decoder {
        private final Decoder delegate;
        int decodeCount = 0;

        CountingDecoder(Decoder delegate) {
            this.delegate = delegate;
        }

        @Override
        public DecodedInstruction decode(int memoryPosition)
                throws InvalidInstructionException {
            decodeCount++;
            return delegate.decode(memoryPosition);
        }
    }

    private static EdigenTestCompiler.Compiled compiled;

    private FakeByteMemory memory;
    private CountingDecoder countingDecoder;
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
        // Fill default valid instruction at every address we'll use
        for (int addr = 0; addr < 2100; addr++) {
            memory.writeBytes(addr, new byte[]{0x00});
        }

        Decoder realDecoder = compiled.newDecoder(memory);
        countingDecoder = new CountingDecoder(realDecoder);
        disassembler = compiled.newDisassembler(memory, countingDecoder);
    }

    // ------- Core double-decode fix -------

    @Test
    public void testDisassembleThenGetNext_decodesOnce() throws Exception {
        disassembler.disassemble(0x100);
        disassembler.getNextInstructionPosition(0x100);

        assertEquals(
                "disassemble + getNext for same address: only 1 decode",
                1, countingDecoder.decodeCount);
    }

    // ------- Sequential disassembly loop -------

    @Test
    public void testSequentialDisassemblyLoop_oneDecodePerInstruction()
            throws Exception {
        int instructionCount = 1000;

        // Simulate typical disassembly loop:
        //   for each address:
        //     disassemble(addr)
        //     addr = getNextInstructionPosition(addr)
        int addr = 0;
        for (int i = 0; i < instructionCount; i++) {
            disassembler.disassemble(addr);
            addr = disassembler.getNextInstructionPosition(addr);
        }

        assertEquals(
                "Cached: 1 decode per instruction in loop",
                instructionCount, countingDecoder.decodeCount);
    }

    // ------- Different addresses invalidate cache -------

    @Test
    public void testDifferentAddress_invalidatesCache() throws Exception {
        disassembler.disassemble(0x100);
        disassembler.disassemble(0x200);

        assertEquals(
                "Different addresses should each trigger a decode",
                2, countingDecoder.decodeCount);
    }

    @Test
    public void testDifferentAddress_thenGetNext_newAddressCached()
            throws Exception {
        disassembler.disassemble(0x100);
        // Moving to a different address
        disassembler.disassemble(0x200);
        disassembler.getNextInstructionPosition(0x200);

        assertEquals(
                "getNext for new address uses its cached value",
                2, countingDecoder.decodeCount);
    }

    @Test
    public void testGetNextThenDisassemble_alsoWorks() throws Exception {
        // Reversed order — getNext first, then disassemble
        disassembler.getNextInstructionPosition(0x100);
        disassembler.disassemble(0x100);

        assertEquals(
                "Cache works regardless of call order",
                1, countingDecoder.decodeCount);
    }

    // ------- Edge cases -------

    @Test
    public void testAddressZero_cached() throws Exception {
        disassembler.disassemble(0);
        disassembler.getNextInstructionPosition(0);

        assertEquals(
                "Address 0 should also be cached",
                1, countingDecoder.decodeCount);
    }

    @Test
    public void testRepeatedDisassembleOnly_sameAddress() throws Exception {
        disassembler.disassemble(0x100);
        disassembler.disassemble(0x100);
        disassembler.disassemble(0x100);

        assertEquals(
                "Repeated disassemble at same address: 1 decode",
                1, countingDecoder.decodeCount);
    }

    @Test
    public void testAlternatingAddresses_noCacheBenefit() throws Exception {
        // Alternating between 2 addresses: cache holds only 1
        for (int i = 0; i < 100; i++) {
            disassembler.disassemble(0x100);
            disassembler.disassemble(0x200);
        }

        assertEquals(
                "Alternating addresses: each switch forces a decode",
                200, countingDecoder.decodeCount);
    }
}
