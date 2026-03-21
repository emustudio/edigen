/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * Tests the verify-on-read LRU cache pattern used in the generated
 * Decoder template (Decoder.edt).
 * <p>
 * The cache stores raw instruction bytes alongside each decoded
 * result. On cache hit the current memory bytes are re-read and
 * compared with the stored ones so that self-modifying code is
 * detected automatically — no external listener or manual
 * invalidation needed.
 * <p>
 * The caching logic tested here mirrors the generated code:
 * <pre>
 *   instructionBytes = memory.read(pos, MAX);
 *   CacheEntry e = cache.get(pos);
 *   if (e != null &amp;&amp; Arrays.equals(e.rawBytes, instructionBytes))
 *       return e.instruction;
 *   // ... expensive decoding ...
 *   cache.put(pos, new CacheEntry(instructionBytes, instr));
 * </pre>
 */
public class DecoderCacheTest {

    // --- Fakes mirroring the generated template types ---

    private static class FakeDecodedInstruction {
        final int address;

        FakeDecodedInstruction(int address) {
            this.address = address;
        }
    }

    private static class CacheEntry {
        final byte[] rawBytes;
        final FakeDecodedInstruction instruction;

        CacheEntry(byte[] rawBytes, FakeDecodedInstruction instr) {
            this.rawBytes = rawBytes;
            this.instruction = instr;
        }
    }

    /**
     * Simple fake memory: each address maps to a byte array of
     * MAX_INSTRUCTION_BYTES. Defaults to all-zeros.
     */
    private static class FakeMemory {
        final int maxBytes;
        private final Map<Integer, byte[]> data = new HashMap<>();

        FakeMemory(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        byte[] read(int position) {
            byte[] stored = data.get(position);
            return stored != null
                    ? Arrays.copyOf(stored, maxBytes)
                    : new byte[maxBytes];
        }

        void write(int position, byte[] bytes) {
            data.put(position, Arrays.copyOf(bytes, maxBytes));
        }
    }

    /**
     * Simulates the generated decoder with verify-on-read LRU
     * cache from Decoder.edt.
     */
    private static class CachedDecoder {
        private static final int CACHE_CAPACITY = 256;

        int decodeCount = 0;
        int memoryReadCount = 0;
        final FakeMemory memory;

        private final Map<Integer, CacheEntry> cache =
                new LinkedHashMap<Integer, CacheEntry>(
                        CACHE_CAPACITY + 1, 0.75f, true
                ) {
                    @Override
                    protected boolean removeEldestEntry(
                            Map.Entry<Integer, CacheEntry> eldest) {
                        return size() > CACHE_CAPACITY;
                    }
                };

        CachedDecoder(FakeMemory memory) {
            this.memory = memory;
        }

        FakeDecodedInstruction decode(int memoryPosition) {
            // Always read memory (mirrors the template)
            byte[] instrBytes = memory.read(memoryPosition);
            memoryReadCount++;

            CacheEntry entry = cache.get(memoryPosition);
            if (entry != null
                    && Arrays.equals(entry.rawBytes, instrBytes)) {
                return entry.instruction;
            }

            // Expensive path: full decode
            decodeCount++;
            FakeDecodedInstruction instruction =
                    new FakeDecodedInstruction(memoryPosition);
            cache.put(memoryPosition,
                    new CacheEntry(instrBytes, instruction));
            return instruction;
        }
    }

    /**
     * Simulates the old Decoder.edt (no cache).
     */
    private static class UncachedDecoder {
        int decodeCount = 0;
        final FakeMemory memory;

        UncachedDecoder(FakeMemory memory) {
            this.memory = memory;
        }

        FakeDecodedInstruction decode(int memoryPosition) {
            memory.read(memoryPosition);
            decodeCount++;
            return new FakeDecodedInstruction(memoryPosition);
        }
    }

    private FakeMemory memory;
    private CachedDecoder cachedDecoder;
    private UncachedDecoder uncachedDecoder;

    @Before
    public void setUp() {
        memory = new FakeMemory(4);
        cachedDecoder = new CachedDecoder(memory);
        uncachedDecoder = new UncachedDecoder(memory);
    }

    // ------- Basic cache behavior -------

    @Test
    public void testSingleDecode_noExtraWork() {
        cachedDecoder.decode(0x100);
        assertEquals(1, cachedDecoder.decodeCount);
    }

    @Test
    public void testRepeatedDecode_sameAddress_onlyOneRealDecode() {
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x100);

        assertEquals(
                "Repeated decode at same address should hit cache",
                1, cachedDecoder.decodeCount);
    }

    @Test
    public void testRepeatedDecode_returnsSameInstance() {
        FakeDecodedInstruction first = cachedDecoder.decode(0x100);
        FakeDecodedInstruction second = cachedDecoder.decode(0x100);

        assertSame(
                "Cache should return the exact same object",
                first, second);
    }

    @Test
    public void testDifferentAddresses_eachDecodedOnce() {
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x200);
        cachedDecoder.decode(0x300);
        assertEquals(3, cachedDecoder.decodeCount);

        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x200);
        cachedDecoder.decode(0x300);

        assertEquals(
                "Second pass should be fully cached",
                3, cachedDecoder.decodeCount);
    }

    @Test
    public void testEmulatorTightLoop_massiveReduction() {
        int loopIterations = 10_000;
        int loopBodyInstructions = 5;

        for (int iter = 0; iter < loopIterations; iter++) {
            for (int addr = 0; addr < loopBodyInstructions; addr++) {
                cachedDecoder.decode(addr);
            }
        }

        assertEquals(
                "Only 5 real decodes for 50,000 decode() calls",
                loopBodyInstructions, cachedDecoder.decodeCount);

        for (int iter = 0; iter < loopIterations; iter++) {
            for (int addr = 0; addr < loopBodyInstructions; addr++) {
                uncachedDecoder.decode(addr);
            }
        }

        assertEquals(
                "Uncached decoder decodes every single call",
                loopIterations * loopBodyInstructions,
                uncachedDecoder.decodeCount);
    }


    // ------- LRU eviction -------

    @Test
    public void testLRUEviction_exceedCapacity() {
        for (int i = 0; i < 300; i++) {
            cachedDecoder.decode(i);
        }
        assertEquals(300, cachedDecoder.decodeCount);

        cachedDecoder.decode(0);
        assertEquals(
                "Evicted entry should require re-decode",
                301, cachedDecoder.decodeCount);

        cachedDecoder.decode(299);
        assertEquals(
                "Most recent entry should still be cached",
                301, cachedDecoder.decodeCount);
    }

    @Test
    public void testLRUEviction_accessOrderPreservesRecent() {
        for (int i = 0; i < 256; i++) {
            cachedDecoder.decode(i);
        }
        assertEquals(256, cachedDecoder.decodeCount);

        cachedDecoder.decode(0);
        assertEquals(256, cachedDecoder.decodeCount);

        cachedDecoder.decode(999);
        assertEquals(257, cachedDecoder.decodeCount);

        cachedDecoder.decode(0);
        assertEquals(
                "Recently accessed entry should survive eviction",
                257, cachedDecoder.decodeCount);

        cachedDecoder.decode(1);
        assertEquals(
                "LRU entry should have been evicted",
                258, cachedDecoder.decodeCount);
    }

    // --- Automatic self-modifying code detection ---

    @Test
    public void testSelfModifyingCode_bytesChanged_forcesRedecode() {
        memory.write(0x100, new byte[]{1, 2, 3, 4});
        cachedDecoder.decode(0x100);
        assertEquals(1, cachedDecoder.decodeCount);

        // Modify memory at the same address
        memory.write(0x100, new byte[]{-1, 2, 3, 4});
        cachedDecoder.decode(0x100);

        assertEquals(
                "Changed bytes at same address must re-decode",
                2, cachedDecoder.decodeCount);
    }

    @Test
    public void testSelfModifyingCode_returnsNewInstance() {
        memory.write(0x100, new byte[]{1, 2, 3, 4});
        FakeDecodedInstruction first = cachedDecoder.decode(0x100);

        memory.write(0x100, new byte[]{-1, 2, 3, 4});
        FakeDecodedInstruction second = cachedDecoder.decode(0x100);

        assertNotSame(
                "Modified bytes must produce a new instruction",
                first, second);
    }

    @Test
    public void testSelfModifyingCode_unchangedBytesStillCached() {
        memory.write(0x100, new byte[]{1, 2, 3, 4});
        cachedDecoder.decode(0x100);

        // "Write" the exact same bytes back
        memory.write(0x100, new byte[]{1, 2, 3, 4});
        cachedDecoder.decode(0x100);

        assertEquals(
                "Same bytes written back should still hit cache",
                1, cachedDecoder.decodeCount);
    }

    @Test
    public void testSelfModifyingCode_otherAddressUnaffected() {
        memory.write(0x100, new byte[]{1, 2, 3, 4});
        memory.write(0x200, new byte[]{0x10, 0x20, 0x30, 0x40});
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x200);
        assertEquals(2, cachedDecoder.decodeCount);

        // Modify only 0x100
        memory.write(0x100, new byte[]{-1, 2, 3, 4});

        cachedDecoder.decode(0x100);  // must re-decode
        cachedDecoder.decode(0x200);  // still cached
        assertEquals(
                "Only the modified address should re-decode",
                3, cachedDecoder.decodeCount);
    }

    @Test
    public void testSelfModifyingCode_loopWithWrite() {
        // Tight loop: address 0 is stable, address 1 is
        // self-modified each iteration.
        memory.write(0, new byte[]{1, 0, 0, 0});
        memory.write(1, new byte[]{2, 0, 0, 0});

        for (int iter = 0; iter < 100; iter++) {
            cachedDecoder.decode(0);
            cachedDecoder.decode(1);
            // Self-modifying code at address 1
            memory.write(1, new byte[]{
                    (byte) (iter + 3), 0, 0, 0});
        }

        // Address 0: decoded once (never changed)
        // Address 1: decoded 100 times (changed every iter)
        assertEquals(
                "Stable instr decoded once, modified one "
                        + "re-decoded each iteration",
                101, cachedDecoder.decodeCount);
    }

    @Test
    public void testMemoryReadAlwaysHappens() {
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x100);

        assertEquals(
                "Memory is read on every call (for verification)",
                3, cachedDecoder.memoryReadCount);
        assertEquals(
                "But actual decoding happens only once",
                1, cachedDecoder.decodeCount);
    }
}
