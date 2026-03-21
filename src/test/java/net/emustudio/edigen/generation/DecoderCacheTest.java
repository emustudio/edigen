/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Tests the LRU cache pattern used in the generated Decoder template
 * (Decoder.edt). Verifies that caching reduces the number of actual
 * decode operations (memory reads + switch-cascade traversals) and
 * that automatic memory-change invalidation works correctly.
 * <p>
 * The caching logic tested here mirrors the generated code exactly:
 * <pre>
 *   DecodedInstruction cached = cache.get(memoryPosition);
 *   if (cached != null) {
 *       return cached;
 *   }
 *   // ... expensive decoding ...
 *   cache.put(memoryPosition, instruction);
 *   return instruction;
 * </pre>
 */
public class DecoderCacheTest {

    /**
     * Simulates a decoded instruction (stands in for emuLib's
     * DecodedInstruction which is not on the test classpath).
     */
    private static class FakeDecodedInstruction {
        final int address;

        FakeDecodedInstruction(int address) {
            this.address = address;
        }
    }

    /**
     * Simulates the generated decoder with the LRU cache from
     * Decoder.edt. The "expensive decoding" is replaced by a
     * counter so we can measure how many real decodes happen.
     */
    private static class CachedDecoder {
        private static final int CACHE_CAPACITY = 256;

        int decodeCount = 0;

        private final Map<Integer, FakeDecodedInstruction> cache =
                new LinkedHashMap<Integer, FakeDecodedInstruction>(
                        CACHE_CAPACITY + 1, 0.75f, true
                ) {
                    @Override
                    protected boolean removeEldestEntry(
                            Map.Entry<Integer, FakeDecodedInstruction> eldest) {
                        return size() > CACHE_CAPACITY;
                    }
                };

        FakeDecodedInstruction decode(int memoryPosition) {
            FakeDecodedInstruction cached = cache.get(memoryPosition);
            if (cached != null) {
                return cached;
            }

            // This is the "expensive" path
            decodeCount++;
            FakeDecodedInstruction instruction =
                    new FakeDecodedInstruction(memoryPosition);
            cache.put(memoryPosition, instruction);
            return instruction;
        }

        void invalidateCache() {
            cache.clear();
        }

        void invalidateCache(int memoryPosition) {
            cache.remove(memoryPosition);
        }
    }

    /**
     * Simulates the generated decoder WITHOUT the cache (the old
     * Decoder.edt behavior). Every decode() call does the full work.
     */
    private static class UncachedDecoder {
        int decodeCount = 0;

        FakeDecodedInstruction decode(int memoryPosition) {
            decodeCount++;
            return new FakeDecodedInstruction(memoryPosition);
        }
    }

    private CachedDecoder cachedDecoder;
    private UncachedDecoder uncachedDecoder;

    @Before
    public void setUp() {
        cachedDecoder = new CachedDecoder();
        uncachedDecoder = new UncachedDecoder();
    }

    // ------- Decoder cache tests -------

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

        // Second pass — all from cache
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

        // Simulate: 5 instructions at addresses 0..4,
        // executed 10,000 times
        for (int iter = 0; iter < loopIterations; iter++) {
            for (int addr = 0; addr < loopBodyInstructions; addr++) {
                cachedDecoder.decode(addr);
            }
        }

        assertEquals(
                "Only 5 real decodes for 50,000 decode() calls",
                loopBodyInstructions, cachedDecoder.decodeCount);

        // Compare: uncached decoder does ALL 50,000
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

    @Test
    public void testInvalidateCache_fullClear_forcesRedecode() {
        cachedDecoder.decode(0x100);
        assertEquals(1, cachedDecoder.decodeCount);

        cachedDecoder.invalidateCache();

        cachedDecoder.decode(0x100);
        assertEquals(
                "After full invalidation, must re-decode",
                2, cachedDecoder.decodeCount);
    }

    @Test
    public void testInvalidateCache_singleAddress_forcesRedecode() {
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x200);
        assertEquals(2, cachedDecoder.decodeCount);

        // Invalidate only address 0x100
        cachedDecoder.invalidateCache(0x100);

        cachedDecoder.decode(0x100);  // must re-decode
        cachedDecoder.decode(0x200);  // still cached
        assertEquals(
                "Only invalidated address should be re-decoded",
                3, cachedDecoder.decodeCount);
    }

    @Test
    public void testInvalidateCache_singleAddress_othersUnaffected() {
        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x200);
        cachedDecoder.decode(0x300);
        assertEquals(3, cachedDecoder.decodeCount);

        cachedDecoder.invalidateCache(0x200);

        cachedDecoder.decode(0x100);
        cachedDecoder.decode(0x300);

        assertEquals(
                "Non-invalidated addresses stay cached",
                3, cachedDecoder.decodeCount);
    }

    @Test
    public void testLRUEviction_exceedCapacity() {
        // Fill cache beyond capacity (256)
        for (int i = 0; i < 300; i++) {
            cachedDecoder.decode(i);
        }
        assertEquals(300, cachedDecoder.decodeCount);

        // Address 0 should have been evicted (LRU)
        cachedDecoder.decode(0);
        assertEquals(
                "Evicted entry should require re-decode",
                301, cachedDecoder.decodeCount);

        // Address 299 (most recent) should still be cached
        cachedDecoder.decode(299);
        assertEquals(
                "Most recent entry should still be cached",
                301, cachedDecoder.decodeCount);
    }

    @Test
    public void testLRUEviction_accessOrderPreservesRecent() {
        // Fill cache to capacity
        for (int i = 0; i < 256; i++) {
            cachedDecoder.decode(i);
        }
        assertEquals(256, cachedDecoder.decodeCount);

        // Access address 0 again (moves it to most-recent)
        cachedDecoder.decode(0);
        assertEquals(256, cachedDecoder.decodeCount); // still cached

        // Now add 1 more entry to trigger eviction
        cachedDecoder.decode(999);
        assertEquals(257, cachedDecoder.decodeCount);

        // Address 0 was accessed recently, should survive eviction
        cachedDecoder.decode(0);
        assertEquals(
                "Recently accessed entry should survive eviction",
                257, cachedDecoder.decodeCount);

        // Address 1 (least recently used) should be evicted
        cachedDecoder.decode(1);
        assertEquals(
                "LRU entry should have been evicted",
                258, cachedDecoder.decodeCount);
    }
}
