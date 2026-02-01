/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.generation.DecoderGenerator;
import net.emustudio.edigen.generation.DisassemblerGenerator;
import net.emustudio.edigen.nodes.Specification;
import net.emustudio.edigen.parser.ParseException;
import net.emustudio.edigen.parser.Parser;
import net.emustudio.edigen.passes.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import static net.emustudio.edigen.Setting.*;

/**
 * A translator from the input file in the domain specific language (processor
 * specification) to two output files (instruction decoder and disassembler)
 * in the Java language.
 */
public class Translator {

    private static final PrintStream DEBUG_STREAM = System.out;

    private final Map<Setting, String> settings;

    /**
     * Constructs the translator.
     *
     * @param settings the settings obtained e.g. from the command line
     */
    public Translator(Map<Setting, String> settings) {
        this.settings = settings;
    }

    /**
     * Reads the input file, transforms the tree and generates the code.
     *
     * @throws IOException       when the file can not be read / written
     * @throws ParseException    when the input file can not be parsed
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void translate() throws IOException, ParseException, SemanticException {
        try (BufferedReader input = new BufferedReader(new FileReader(settings.get(SPECIFICATION)))) {
            Parser parser = new Parser(input);
            Specification specification = parser.parse();
            transform(specification);

            DecoderGenerator decoder = new DecoderGenerator(
                    specification.getDecoder(),
                    settings.get(DECODER_NAME)
            );
            decoder.setOutputDirectory(settings.get(DECODER_DIRECTORY));
            decoder.setTemplateFile(settings.get(DECODER_TEMPLATE));
            decoder.generate();

            DisassemblerGenerator disassembler = new DisassemblerGenerator(
                    specification.getDisassembler(),
                    settings.get(DISASSEMBLER_NAME),
                    settings.get(DECODER_NAME)
            );
            disassembler.setOutputDirectory(settings.get(DISASSEMBLER_DIRECTORY));
            disassembler.setTemplateFile(settings.get(DISASSEMBLER_TEMPLATE));
            disassembler.generate();
        }
    }

    /**
     * Transforms the tree to the form suitable for code generation.
     *
     * @param specification the root AST node
     * @throws SemanticException when a semantic error occurs
     */
    private void transform(Specification specification) throws SemanticException {
        Visitor detectUnusedRulesVisitor;
        if (settings.containsKey(IGNORE_UNUSED_RULES)) {
            detectUnusedRulesVisitor = new Visitor() {
            };
        } else {
            detectUnusedRulesVisitor = new DetectUnusedRulesVisitor();
        }

        Visitor[] transforms = {
                new ResolveNamesVisitor(),
                new DetectRootRulesVisitor(),
                detectUnusedRulesVisitor,
                new SemanticCheckVisitor(),
                new MergePatternsVisitor(),
                new SortVisitor(),
                new SplitVisitor(),
                new PushDownVariantsVisitor(),
                new GroupVisitor(),
                new DetectAmbiguousVisitor(),
                new NarrowMasksVisitor(),
                new RemoveUnreachablePatternsVisitor(),
                new DetectUnreachableFormatsVisitor()
        };

        if (settings.containsKey(DEBUG))
            System.out.println("Debug mode is on. Tree dump:\n");

        for (Visitor visitor : transforms) {
            specification.accept(visitor);

            if (settings.containsKey(DEBUG))
                specification.dump(DEBUG_STREAM);
        }
    }
}
