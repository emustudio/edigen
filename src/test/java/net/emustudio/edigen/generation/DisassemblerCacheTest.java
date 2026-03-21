/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Tests the last-position cache pattern used in the generated
 * Disassembler template (Disassembler.edt). Verifies that the
 * disassembler's cachedDecode() avoids redundant decoder.decode()
 * calls when disassemble() and getNextInstructionPosition() are
 * called for the same memory address.
 * <p>
 * The caching logic tested here mirrors the generated code exactly:
 * <pre>
 *   private DecodedInstruction cachedDecode(int pos) {
 *       if (pos != lastDecodedPosition
 *               || lastDecodedInstruction == null) {
 *           lastDecodedInstruction = decoder.decode(pos);
 *           lastDecodedPosition = pos;
 *       }
 *       return lastDecodedInstruction;
 *   }
 * </pre>
 */
public class DisassemblerCacheTest {

    /**
     * Simulates a decoded instruction.
     */
    private static class FakeDecodedInstruction {
        final int address;
        final int length;

        FakeDecodedInstruction(int address, int length) {
            this.address = address;
            this.length = length;
        }
    }

    /**
     * Simulates a decoder whose decode() calls are counted.
     */
    private static class CountingDecoder {
        int decodeCount = 0;

        FakeDecodedInstruction decode(int memoryPosition) {
            decodeCount++;
            // Simulate: each instruction is 2 bytes long
            return new FakeDecodedInstruction(memoryPosition, 2);
        }
    }

    /**
     * Disassembler WITHOUT cache — the old Disassembler.edt behavior.
     * Both disassemble() and getNextInstructionPosition() call
     * decoder.decode() directly.
     */
    private static class UncachedDisassembler {
        private final CountingDecoder decoder;

        UncachedDisassembler(CountingDecoder decoder) {
            this.decoder = decoder;
        }

        String disassemble(int memoryPosition) {
            FakeDecodedInstruction instr =
                    decoder.decode(memoryPosition);
            return "instr@" + instr.address;
        }

        int getNextInstructionPosition(int memoryPosition) {
            return memoryPosition
                    + decoder.decode(memoryPosition).length;
        }
    }

    /**
     * Disassembler WITH cache — the new Disassembler.edt behavior.
     * Uses cachedDecode() to avoid redundant decoder.decode() calls.
     */
    private static class CachedDisassembler {
        private final CountingDecoder decoder;
        private int lastDecodedPosition = -1;
        private FakeDecodedInstruction lastDecodedInstruction;

        CachedDisassembler(CountingDecoder decoder) {
            this.decoder = decoder;
        }

        String disassemble(int memoryPosition) {
            FakeDecodedInstruction instr =
                    cachedDecode(memoryPosition);
            return "instr@" + instr.address;
        }

        int getNextInstructionPosition(int memoryPosition) {
            return memoryPosition
                    + cachedDecode(memoryPosition).length;
        }

        private FakeDecodedInstruction cachedDecode(
                int memoryPosition) {
            if (memoryPosition != lastDecodedPosition
                    || lastDecodedInstruction == null) {
                lastDecodedInstruction =
                        decoder.decode(memoryPosition);
                lastDecodedPosition = memoryPosition;
            }
            return lastDecodedInstruction;
        }
    }

    private CountingDecoder decoderForCached;
    private CountingDecoder decoderForUncached;
    private CachedDisassembler cachedDisasm;
    private UncachedDisassembler uncachedDisasm;

    @Before
    public void setUp() {
        decoderForCached = new CountingDecoder();
        decoderForUncached = new CountingDecoder();
        cachedDisasm = new CachedDisassembler(decoderForCached);
        uncachedDisasm = new UncachedDisassembler(decoderForUncached);
    }

    // ------- Core double-decode fix -------

    @Test
    public void testDisassembleThenGetNext_uncached_decodestwice() {
        uncachedDisasm.disassemble(0x100);
        uncachedDisasm.getNextInstructionPosition(0x100);

        assertEquals(
                "Old behavior: 2 decode calls for same address",
                2, decoderForUncached.decodeCount);
    }

    @Test
    public void testDisassembleThenGetNext_cached_decodesOnce() {
        cachedDisasm.disassemble(0x100);
        cachedDisasm.getNextInstructionPosition(0x100);

        assertEquals(
                "New behavior: only 1 decode call for same address",
                1, decoderForCached.decodeCount);
    }

    @Test
    public void testDisassembleThenGetNext_50percentReduction() {
        // The typical disassembly loop pattern
        cachedDisasm.disassemble(0x100);
        cachedDisasm.getNextInstructionPosition(0x100);

        uncachedDisasm.disassemble(0x100);
        uncachedDisasm.getNextInstructionPosition(0x100);

        assertEquals(
                "Cached should use exactly half the decode calls",
                decoderForUncached.decodeCount / 2,
                decoderForCached.decodeCount);
    }

    // ------- Sequential disassembly loop -------

    @Test
    public void testSequentialDisassemblyLoop_halfTheDecodeCalls() {
        int instructionCount = 1000;

        // Simulate typical disassembly loop:
        //   for each address:
        //     disassemble(addr)
        //     addr = getNextInstructionPosition(addr)
        int addr = 0;
        for (int i = 0; i < instructionCount; i++) {
            cachedDisasm.disassemble(addr);
            addr = cachedDisasm.getNextInstructionPosition(addr);
        }

        assertEquals(
                "Cached: 1 decode per instruction in loop",
                instructionCount, decoderForCached.decodeCount);

        addr = 0;
        for (int i = 0; i < instructionCount; i++) {
            uncachedDisasm.disassemble(addr);
            addr = uncachedDisasm.getNextInstructionPosition(addr);
        }

        assertEquals(
                "Uncached: 2 decodes per instruction in loop",
                instructionCount * 2, decoderForUncached.decodeCount);
    }

    // ------- Different addresses invalidate cache -------

    @Test
    public void testDifferentAddress_invalidatesCache() {
        cachedDisasm.disassemble(0x100);
        cachedDisasm.disassemble(0x200);

        assertEquals(
                "Different addresses should each trigger a decode",
                2, decoderForCached.decodeCount);
    }

    @Test
    public void testDifferentAddress_thenGetNext_newAddressCached() {
        cachedDisasm.disassemble(0x100);
        // Moving to a different address
        cachedDisasm.disassemble(0x200);
        cachedDisasm.getNextInstructionPosition(0x200);

        assertEquals(
                "getNext for new address uses its cached value",
                2, decoderForCached.decodeCount);
    }

    @Test
    public void testGetNextThenDisassemble_alsoWorks() {
        // Reversed order — getNext first, then disassemble
        cachedDisasm.getNextInstructionPosition(0x100);
        cachedDisasm.disassemble(0x100);

        assertEquals(
                "Cache works regardless of call order",
                1, decoderForCached.decodeCount);
    }

    // ------- Edge cases -------

    @Test
    public void testAddressZero_cached() {
        cachedDisasm.disassemble(0);
        cachedDisasm.getNextInstructionPosition(0);

        assertEquals(
                "Address 0 should also be cached",
                1, decoderForCached.decodeCount);
    }

    @Test
    public void testNegativeAddress_cached() {
        // Some systems might use negative (signed int) addresses
        cachedDisasm.disassemble(-1);
        cachedDisasm.getNextInstructionPosition(-1);

        assertEquals(
                "Negative addresses should also be cached",
                1, decoderForCached.decodeCount);
    }

    @Test
    public void testRepeatedDisassembleOnly_sameAddress() {
        cachedDisasm.disassemble(0x100);
        cachedDisasm.disassemble(0x100);
        cachedDisasm.disassemble(0x100);

        assertEquals(
                "Repeated disassemble at same address: 1 decode",
                1, decoderForCached.decodeCount);
    }

    @Test
    public void testAlternatingAddresses_noCacheBenefit() {
        // Alternating between 2 addresses: cache holds only 1
        for (int i = 0; i < 100; i++) {
            cachedDisasm.disassemble(0x100);
            cachedDisasm.disassemble(0x200);
        }

        assertEquals(
                "Alternating addresses: each switch forces a decode",
                200, decoderForCached.decodeCount);
    }
}

