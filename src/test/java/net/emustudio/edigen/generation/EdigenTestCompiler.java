/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.Translator;
import net.emustudio.edigen.ConsoleGenerationLog;
import net.emustudio.emulib.plugin.cpu.Decoder;
import net.emustudio.emulib.plugin.memory.MemoryContext;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static net.emustudio.edigen.Setting.*;

/**
 * Test helper that runs the full Edigen pipeline (parse → transform →
 * generate) against the real {@code Decoder.edt} template, compiles the generated Java
 * sources at test-time and returns loaded {@link Class} objects.
 * <p>
 * This is used by {@link DecoderCacheTest} and
 * {@link CapturedInstructionTest} so they exercise the <em>actual</em>
 * generated caching code rather than hand-written fakes.
 */
public class EdigenTestCompiler {

    private static final String DECODER_FQCN = "test.generated.TestDecoder";

    /**
     * Holds the compiled decoder class.
     */
    public static class Compiled implements Closeable {
        private final URLClassLoader classLoader;
        private final Class<?> decoderClass;

        Compiled(URLClassLoader classLoader, Class<?> decoderClass) {
            this.classLoader = classLoader;
            this.decoderClass = decoderClass;
        }

        /**
         * Creates a new decoder instance backed by the given memory.
         */
        public Decoder newDecoder(MemoryContext<? extends Number> memory) {
            try {
                Constructor<?> ctor = decoderClass.getConstructor(MemoryContext.class);
                return (Decoder) ctor.newInstance(memory);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Cannot instantiate decoder", e);
            }
        }

        @Override
        public void close() throws IOException {
            classLoader.close();
        }
    }

    /**
     * Parses the given Edigen specification, generates a decoder from the real template,
     * compiles it and returns the loaded class.
     *
     * @param spec the Edigen specification text
     * @return a {@link Compiled} handle with loaded classes
     */
    public static Compiled compile(String spec) throws Exception {
        // 1. Generate sources through the production pipeline
        Path tmpDir = Files.createTempDirectory("edigen-test-");
        Path packageDir = tmpDir.resolve("test").resolve("generated");
        Files.createDirectories(packageDir);
        Path specFile = tmpDir.resolve("spec.eds");
        Files.writeString(specFile, spec, Charset.defaultCharset());

        new Translator(Map.of(
                SPECIFICATION, specFile.toString(),
                DECODER_NAME, DECODER_FQCN,
                DECODER_DIRECTORY, packageDir.toString()
        ), new ConsoleGenerationLog(System.err, false)).translate();

        // 2. Compile generated sources
        Path decoderSrc = packageDir.resolve("TestDecoder.java");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                    "No JavaCompiler available — tests must run on a JDK (not a JRE)");
        }

        String classpath = System.getProperty("edigen.test.classpath",
                System.getProperty("java.class.path"));
        int rc = compiler.run(null, null, null,
                "-classpath", classpath,
                "-d", tmpDir.toString(),
                decoderSrc.toString());
        if (rc != 0) {
            // Read sources for debugging
            String decoderSource = new String(Files.readAllBytes(decoderSrc));
            throw new RuntimeException(
                    "Compilation failed (rc=" + rc + ").\nDecoder source:\n" + decoderSource);
        }

        // 3. Load compiled classes
        URLClassLoader loader = new URLClassLoader(
                new URL[]{tmpDir.toUri().toURL()},
                EdigenTestCompiler.class.getClassLoader());

        Class<?> decoderClass = loader.loadClass(DECODER_FQCN);

        return new Compiled(loader, decoderClass);
    }
}
