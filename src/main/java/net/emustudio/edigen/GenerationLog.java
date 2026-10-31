/* SPDX-FileCopyrightText: 2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import java.util.function.Supplier;

/**
 * Receives messages for one generation invocation. Implementations own filtering
 * and presentation; they must evaluate debug messages synchronously, or discard
 * them without evaluation when debug output is disabled.
 */
public interface GenerationLog {
    void info(String message);

    void warn(String message);

    void debug(Supplier<String> message);
}
