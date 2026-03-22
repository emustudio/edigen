/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;

import java.util.HashMap;
import java.util.Map;

/**
 * A trivial in-memory {@code MemoryContext<Byte>} used by the cache
 * tests.  Each address defaults to {@code 0x00}; callers may
 * {@link #writeBytes(int, byte[])} to set arbitrary content.
 * <p>
 * A {@link #readCount} counter is exposed so tests can verify that
 * the generated decoder reads memory on every {@code decode()} call.
 */
class FakeByteMemory implements MemoryContext<Byte> {

    private final Map<Integer, Byte> data = new HashMap<>();

    /** Number of times {@link #read(int, int)} was called. */
    int readCount = 0;

    // ---- convenience helpers for tests ----

    /**
     * Writes a raw byte array starting at {@code position}.
     */
    void writeBytes(int position, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            data.put(position + i, bytes[i]);
        }
    }

    // ---- MemoryContext implementation ----

    @Override
    public Byte read(int position) {
        return data.getOrDefault(position, (byte) 0);
    }

    @Override
    public Byte[] read(int position, int count) {
        readCount++;
        Byte[] result = new Byte[count];
        for (int i = 0; i < count; i++) {
            result[i] = read(position + i);
        }
        return result;
    }

    @Override
    public void write(int position, Byte value) {
        data.put(position, value);
    }

    @Override
    public void write(int position, Byte[] values, int count) {
        for (int i = 0; i < count; i++) {
            data.put(position + i, values[i]);
        }
    }

    @Override
    public Class<Byte> getCellTypeClass() {
        return Byte.class;
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public int getSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public MemoryContextAnnotations annotations() {
        return null;
    }

    @Override
    public void addMemoryListener(MemoryListener listener) {
    }

    @Override
    public void removeMemoryListener(MemoryListener listener) {
    }

    @Override
    public void setMemoryNotificationsEnabled(boolean enabled) {
    }

    @Override
    public boolean areMemoryNotificationsEnabled() {
        return false;
    }
}
