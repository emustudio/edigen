/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.generation.DecoderGenerator;
import net.emustudio.edigen.generation.DisassemblerGenerator;
import net.emustudio.edigen.nodes.Specification;
import net.emustudio.edigen.parser.ParseException;
import net.emustudio.edigen.parser.Parser;
import net.emustudio.edigen.parser.TokenMgrError;
import net.emustudio.edigen.passes.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static net.emustudio.edigen.Setting.*;

/**
 * A translator from the input file in the domain specific language (processor
 * specification) to two output files (instruction decoder and disassembler)
 * in the Java language.
 */
public class Translator {

    private final Map<Setting, String> settings;
    private final GenerationLog log;

    /**
     * Constructs the translator.
     *
     * @param settings the settings obtained e.g. from the command line
     * @param log receives messages for this invocation
     */
    public Translator(Map<Setting, String> settings, GenerationLog log) {
        this.settings = settings;
        this.log = log;
    }

    /**
     * Reads the input file, transforms the tree and generates the code.
     *
     * @throws IOException       when the file can not be read / written
     * @throws ParseException    when the input file can not be parsed
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void translate() throws IOException, ParseException, SemanticException {
        long start = System.nanoTime();
        Path source = Path.of(settings.get(SPECIFICATION)).toAbsolutePath();
        log.info("Generating decoder and disassembler from " + source);
        Specification specification = parse(source);
        transform(specification, source);

        DecoderGenerator decoder = new DecoderGenerator(
                specification.getDecoder(),
                settings.get(DECODER_NAME), log
        );
        decoder.setOutputDirectory(settings.get(DECODER_DIRECTORY));
        decoder.setTemplateFile(settings.get(DECODER_TEMPLATE));
        decoder.generate();

        DisassemblerGenerator disassembler = new DisassemblerGenerator(
                specification.getDisassembler(),
                settings.get(DISASSEMBLER_NAME),
                settings.get(DECODER_NAME), log
        );
        disassembler.setOutputDirectory(settings.get(DISASSEMBLER_DIRECTORY));
        disassembler.setTemplateFile(settings.get(DISASSEMBLER_TEMPLATE));
        disassembler.generate();
        log.info("Generation completed in " + elapsedMillis(start) + " ms");
    }

    private Specification parse(Path source) throws IOException, ParseException {
        long start = System.nanoTime();
        Specification specification;
        try (BufferedReader input = new BufferedReader(new FileReader(source.toFile()))) {
            specification = new Parser(input).parse();
        } catch (IOException ex) {
            throw new IOException("Cannot read specification " + source + ": " + ex.getMessage(), ex);
        } catch (ParseException | TokenMgrError ex) {
            ParseException contextual = new ParseException(
                    "Cannot parse specification " + source + ": " + ex.getMessage());
            contextual.initCause(ex);
            throw contextual;
        }
        log.debug(() -> "Parsed " + source + " in " + elapsedMillis(start) + " ms");
        return specification;
    }

    /**
     * Transforms the tree to the form suitable for code generation.
     *
     * @param specification the root AST node
     * @throws SemanticException when a semantic error occurs
     */
    private void transform(Specification specification, Path source) throws SemanticException {
        Visitor[] transforms = {
                new ResolveNamesVisitor(),
                new DetectRootRulesVisitor(),
                new DetectUnusedRulesVisitor(),
                new SemanticCheckVisitor(),
                new MergePatternsVisitor(),
                new SortVisitor(),
                new SplitVisitor(),
                new PushDownVariantsVisitor(),
                new GroupVisitor(),
                new DetectAmbiguousVisitor(),
                new NarrowMasksVisitor(),
                new RemoveUnreachablePatternsVisitor(),
                new DetectUnreachableFormatsVisitor(message -> log.warn(source + ": " + message))
        };

        for (Visitor visitor : transforms) {
            if (visitor instanceof DetectUnusedRulesVisitor && settings.containsKey(IGNORE_UNUSED_RULES)) {
                continue;
            }
            String pass = visitor.getClass().getSimpleName();
            log.debug(() -> "Running transformation " + pass);
            long start = System.nanoTime();
            try {
                specification.accept(visitor);
            } catch (SemanticException ex) {
                throw new SemanticException(source + ": Transformation " + pass + " failed", ex);
            }
            log.debug(() -> "Completed transformation " + pass + " in " + elapsedMillis(start) + " ms");

            if (settings.containsKey(DEBUG)) {
                log.debug(() -> {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    try (PrintStream stream = new PrintStream(bytes, false, StandardCharsets.UTF_8)) {
                        specification.dump(stream);
                    }
                    return "AST after " + pass + ":\n" + bytes.toString(StandardCharsets.UTF_8);
                });
            }
        }
    }

    private static long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
