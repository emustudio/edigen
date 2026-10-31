/* SPDX-FileCopyrightText: 2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import java.io.PrintStream;
import java.util.function.Supplier;

/** Writes generation messages to the supplied console stream. */
public final class ConsoleGenerationLog implements GenerationLog {
    private final PrintStream output;
    private final boolean debug;

    public ConsoleGenerationLog(PrintStream output, boolean debug) {
        this.output = output;
        this.debug = debug;
    }

    @Override
    public void info(String message) {
        output.println("INFO  " + message);
    }

    @Override
    public void warn(String message) {
        output.println("WARN  " + message);
    }

    @Override
    public void debug(Supplier<String> message) {
        if (debug) {
            output.println("DEBUG " + message.get());
        }
    }
}
