/*
 * Copyright (C) 2012 Matúš Sulír
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
 * @author Matúš Sulír
 */
public class Translator {
    
    private static final PrintStream DEBUG_STREAM = System.out;
    
    private final Map<Setting, String> settings;

    /**
     * Constructs the translator.
     * @param settings the settings obtained e.g. from the command line
     */
    public Translator(Map<Setting, String> settings) {
        this.settings = settings;
    }
    
    /**
     * Reads the input file, transforms the tree and generates the code.
     * @throws IOException when the file can not be read / written
     * @throws ParseException when the input file can not be parsed
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void translate() throws IOException, ParseException, SemanticException {
        BufferedReader input = null;
        
        try {
            input = new BufferedReader(new FileReader(settings.get(SPECIFICATION)));
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
        } finally {
            if (input != null)
                input.close();
        }
    }
    
    /**
     * Transforms the tree to the form suitable for code generation.
     * @param specification the root AST node
     * @throws SemanticException when a semantic error occurs
     */
    private void transform(Specification specification) throws SemanticException {
        Visitor[] transforms = {
            new ResolveNamesVisitor(),
            new SemanticCheckVisitor(),
            new JoinVisitor(),
            new SortVisitor(),
            new SplitVisitor(),
            new MoveVariantsVisitor(),
            new GroupVisitor(),
            new DetectAmbiguousVisitor(),
            new MoveMasksVisitor(),
            new RemovePatternsVisitor()
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
