/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.Translator;
import net.emustudio.emulib.plugins.cpu.Decoder;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.memory.MemoryContext;

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
 * generate) against the real {@code Decoder.edt} and
 * {@code Disassembler.edt} templates, compiles the generated Java
 * sources at test-time and returns loaded {@link Class} objects.
 * <p>
 * This is used by {@link DecoderCacheTest} and
 * {@link DisassemblerCacheTest} so they exercise the <em>actual</em>
 * generated caching code rather than hand-written fakes.
 */
public class EdigenTestCompiler {

    private static final String DECODER_FQCN = "test.generated.TestDecoder";
    private static final String DISASM_FQCN = "test.generated.TestDisassembler";

    /**
     * Holds the compiled decoder and disassembler classes.
     */
    public static class Compiled implements Closeable {
        private final URLClassLoader classLoader;
        private final Class<?> decoderClass;
        private final Class<?> disassemblerClass;

        Compiled(URLClassLoader classLoader, Class<?> decoderClass, Class<?> disassemblerClass) {
            this.classLoader = classLoader;
            this.decoderClass = decoderClass;
            this.disassemblerClass = disassemblerClass;
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

        /**
         * Creates a new disassembler instance.
         */
        public Disassembler newDisassembler(MemoryContext<? extends Number> memory, Decoder decoder) {
            try {
                Constructor<?> ctor = disassemblerClass.getConstructor(MemoryContext.class, Decoder.class);
                return (Disassembler) ctor.newInstance(memory, decoder);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Cannot instantiate disassembler", e);
            }
        }

        public Class<?> getDecoderClass() {
            return decoderClass;
        }

        public Class<?> getDisassemblerClass() {
            return disassemblerClass;
        }

        @Override
        public void close() throws IOException {
            classLoader.close();
        }
    }

    /**
     * Parses the given Edigen specification, generates decoder and
     * disassembler Java sources from the real {@code .edt} templates,
     * compiles them and returns loaded classes.
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
                DISASSEMBLER_NAME, DISASM_FQCN,
                DECODER_DIRECTORY, packageDir.toString(),
                DISASSEMBLER_DIRECTORY, packageDir.toString()
        )).translate();

        // 2. Compile generated sources
        Path decoderSrc = packageDir.resolve("TestDecoder.java");
        Path disasmSrc = packageDir.resolve("TestDisassembler.java");

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
                decoderSrc.toString(),
                disasmSrc.toString());
        if (rc != 0) {
            // Read sources for debugging
            String decoderSource = new String(Files.readAllBytes(decoderSrc));
            String disasmSource = new String(Files.readAllBytes(disasmSrc));
            throw new RuntimeException(
                    "Compilation failed (rc=" + rc + ").\nDecoder source:\n" + decoderSource
                            + "\nDisassembler source:\n" + disasmSource);
        }

        // 3. Load compiled classes
        URLClassLoader loader = new URLClassLoader(
                new URL[]{tmpDir.toUri().toURL()},
                EdigenTestCompiler.class.getClassLoader());

        Class<?> decoderClass = loader.loadClass(DECODER_FQCN);
        Class<?> disasmClass = loader.loadClass(DISASM_FQCN);

        return new Compiled(loader, decoderClass, disasmClass);
    }
}
