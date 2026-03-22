/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;
import net.emustudio.emulib.plugins.cpu.Decoder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the verify-on-read LRU cache in the <em>real</em> generated
 * decoder produced from {@code Decoder.edt}.
 * <p>
 * The test compiles a minimal Edigen specification through the full
 * pipeline (parse → transform → generate → javac) at class-load time,
 * then exercises the resulting {@link Decoder} implementation directly.
 * <p>
 * Caching contract (from the template):
 * <ul>
 *   <li>Memory is always read on every {@code decode()} call.</li>
 *   <li>On cache hit (same address, same bytes) the <em>same</em>
 *       {@link DecodedInstruction} object is returned.</li>
 *   <li>If the bytes at a cached address change (self-modifying code)
 *       a fresh decode is performed automatically.</li>
 *   <li>The cache is LRU with capacity 256.</li>
 * </ul>
 */
public class DecoderCacheTest {

    /**
     * Minimal Edigen specification: two 1-byte instructions.
     * <ul>
     *   <li>{@code 0x00} → "nop"</li>
     *   <li>{@code 0xFF} → "halt"</li>
     * </ul>
     */
    private static final String SPEC =
            "root instruction;\n"
                    + "instruction = \"nop\": 0x00 | \"halt\": 0xFF;\n"
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
        compiled.close();
    }

    @Before
    public void setUp() {
        memory = new FakeByteMemory();
        decoder = compiled.newDecoder(memory);
    }

    // ------- Basic cache behavior -------

    @Test
    public void testSingleDecode() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});
        DecodedInstruction instr = decoder.decode(0x100);
        assertNotNull(instr);
    }

    @Test
    public void testRepeatedDecode_returnsSameInstance() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});

        DecodedInstruction first = decoder.decode(0x100);
        DecodedInstruction second = decoder.decode(0x100);
        DecodedInstruction third = decoder.decode(0x100);

        assertSame("Cache should return the exact same object", first, second);
        assertSame("Cache should return the exact same object", first, third);
    }

    @Test
    public void testDifferentAddresses_differentInstances() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});
        memory.writeBytes(0x200, new byte[]{(byte) 0xFF});

        DecodedInstruction a = decoder.decode(0x100);
        DecodedInstruction b = decoder.decode(0x200);

        assertNotSame("Different addresses should produce different objects", a, b);
    }

    @Test
    public void testDifferentAddresses_eachCachedSeparately() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});
        memory.writeBytes(0x200, new byte[]{(byte) 0xFF});
        memory.writeBytes(0x300, new byte[]{0x00});

        DecodedInstruction a1 = decoder.decode(0x100);
        DecodedInstruction b1 = decoder.decode(0x200);
        DecodedInstruction c1 = decoder.decode(0x300);

        DecodedInstruction a2 = decoder.decode(0x100);
        DecodedInstruction b2 = decoder.decode(0x200);
        DecodedInstruction c2 = decoder.decode(0x300);

        assertSame("Second pass should hit cache for 0x100", a1, a2);
        assertSame("Second pass should hit cache for 0x200", b1, b2);
        assertSame("Second pass should hit cache for 0x300", c1, c2);
    }

    @Test
    public void testEmulatorTightLoop_allCacheHits() throws Exception {
        int loopBodyInstructions = 5;
        for (int addr = 0; addr < loopBodyInstructions; addr++) {
            memory.writeBytes(addr, new byte[]{0x00});
        }

        DecodedInstruction[] firstPass = new DecodedInstruction[loopBodyInstructions];
        for (int addr = 0; addr < loopBodyInstructions; addr++) {
            firstPass[addr] = decoder.decode(addr);
        }

        for (int iter = 0; iter < 10_000; iter++) {
            for (int addr = 0; addr < loopBodyInstructions; addr++) {
                DecodedInstruction cached = decoder.decode(addr);
                assertSame(
                        "Tight loop iteration " + iter + " addr " + addr + " should hit cache",
                        firstPass[addr], cached);
            }
        }
    }

    // ------- LRU eviction -------

    @Test
    public void testLRUEviction_exceedCapacity() throws Exception {
        // Fill 300 entries (capacity is 256) — first 44 will be evicted
        DecodedInstruction[] initial = new DecodedInstruction[300];
        for (int i = 0; i < 300; i++) {
            memory.writeBytes(i, new byte[]{0x00});
            initial[i] = decoder.decode(i);
        }

        // Address 0 was evicted → re-decode should produce a new object
        DecodedInstruction reDecoded = decoder.decode(0);
        assertNotSame("Evicted entry should produce a new object", initial[0], reDecoded);

        // Address 299 is the most recent → still cached
        DecodedInstruction stillCached = decoder.decode(299);
        assertSame("Most recent entry should still be cached", initial[299], stillCached);
    }

    @Test
    public void testLRUEviction_accessOrderPreservesRecent() throws Exception {
        // Fill exactly 256 entries
        DecodedInstruction[] initial = new DecodedInstruction[256];
        for (int i = 0; i < 256; i++) {
            memory.writeBytes(i, new byte[]{0x00});
            initial[i] = decoder.decode(i);
        }

        // "Touch" address 0 to make it recently used
        DecodedInstruction touched = decoder.decode(0);
        assertSame(initial[0], touched);

        // Add a new entry (999) → evicts LRU, which should be address 1
        memory.writeBytes(999, new byte[]{0x00});
        decoder.decode(999);

        // Address 0 should survive (was recently accessed)
        DecodedInstruction survived = decoder.decode(0);
        assertSame("Recently accessed entry should survive eviction", initial[0], survived);

        // Address 1 should have been evicted
        DecodedInstruction evicted = decoder.decode(1);
        assertNotSame("LRU entry should have been evicted", initial[1], evicted);
    }

    // ------- Automatic self-modifying code detection -------

    @Test
    public void testSelfModifyingCode_bytesChanged_forcesRedecode() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});
        DecodedInstruction first = decoder.decode(0x100);

        // Modify memory at the same address
        memory.writeBytes(0x100, new byte[]{(byte) 0xFF});
        DecodedInstruction second = decoder.decode(0x100);

        assertNotSame("Changed bytes at same address must produce a new instruction", first, second);
    }

    @Test
    public void testSelfModifyingCode_unchangedBytesStillCached() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});
        DecodedInstruction first = decoder.decode(0x100);

        // "Write" the exact same bytes back
        memory.writeBytes(0x100, new byte[]{0x00});
        DecodedInstruction second = decoder.decode(0x100);

        assertSame("Same bytes written back should still hit cache", first, second);
    }

    @Test
    public void testSelfModifyingCode_otherAddressUnaffected() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});
        memory.writeBytes(0x200, new byte[]{(byte) 0xFF});

        DecodedInstruction a = decoder.decode(0x100);
        DecodedInstruction b = decoder.decode(0x200);

        // Modify only 0x100
        memory.writeBytes(0x100, new byte[]{(byte) 0xFF});

        DecodedInstruction a2 = decoder.decode(0x100);
        DecodedInstruction b2 = decoder.decode(0x200);

        assertNotSame("Modified address should produce new object", a, a2);
        assertSame("Unmodified address should still be cached", b, b2);
    }

    @Test
    public void testSelfModifyingCode_loopWithWrite() throws Exception {
        // Address 0: stable instruction
        memory.writeBytes(0, new byte[]{0x00});
        DecodedInstruction stableFirst = decoder.decode(0);

        // Address 1: modified every iteration
        memory.writeBytes(1, new byte[]{0x00});

        DecodedInstruction prevAtAddr1 = decoder.decode(1);
        int reDecodedCount = 0;

        for (int iter = 0; iter < 100; iter++) {
            DecodedInstruction stable = decoder.decode(0);
            assertSame("Stable instruction should always be cached", stableFirst, stable);

            // Self-modify address 1 — alternate between 0x00 and 0xFF
            byte newByte = (iter % 2 == 0) ? (byte) 0xFF : 0x00;
            memory.writeBytes(1, new byte[]{newByte});
            DecodedInstruction modified = decoder.decode(1);

            if (modified != prevAtAddr1) {
                reDecodedCount++;
            }
            prevAtAddr1 = modified;
        }

        assertEquals(
                "Address 1 should be re-decoded every iteration (byte changes each time)",
                100, reDecodedCount);
    }

    @Test
    public void testMemoryReadAlwaysHappens() throws Exception {
        memory.writeBytes(0x100, new byte[]{0x00});

        decoder.decode(0x100);
        decoder.decode(0x100);
        decoder.decode(0x100);

        assertEquals(
                "Memory is read on every decode() call (for verification)",
                3, memory.readCount);
    }
}
