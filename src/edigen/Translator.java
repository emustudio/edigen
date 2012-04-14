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
package edigen;

import static edigen.Setting.*;
import edigen.decoder.DecoderGenerator;
import edigen.disasm.DisassemblerGenerator;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.tree.Specification;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * A translator from the input file in the domain specific language (processor
 * specification) to two output files (instruction decoder and disassembler)
 * in the Java language.
 * @author Matúš Sulír
 */
public class Translator {
    
    private static final PrintStream DEBUG_STREAM = System.out;
    
    private Map<Setting, String> settings;

    /**
     * Constructs the translator.
     * @param settings the settings obtained e.g. from the command line
     */
    public Translator(Map<Setting, String> settings) {
        this.settings = settings;
    }
    
    /**
     * Performs the translation process.
     * @throws IOException when the file can not be read / written
     * @throws ParseException when the input file can not be parsed
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void translate() throws IOException, ParseException, SemanticException {
        BufferedReader input = null;
        
        try {
            input = new BufferedReader(new FileReader(settings.get(SPECIFICATION)));
            Parser p = new Parser(input);
            Specification specification = p.parse();

            if (settings.containsKey(DEBUG))
                specification.dump(DEBUG_STREAM);

            Generator decoder = new DecoderGenerator(specification,
                    settings.get(DECODER_CLASS));
            decoder.setTemplateFile(settings.get(DECODER_TEMPLATE));
            
            Generator disassembler = new DisassemblerGenerator(specification,
                    settings.get(DISASSEMBLER_CLASS));
            disassembler.setTemplateFile(settings.get(DISASSEMBLER_TEMPLATE));
            
            for (Generator generator : new Generator[] {decoder, disassembler}) {
                generator.setPackageName(settings.get(PACKAGE));
                generator.setOutputDirectory(settings.get(OUTPUT_DIRECTORY));
                generator.setDebugStream(settings.containsKey(DEBUG) ? DEBUG_STREAM : null);
                
                generator.transform();
                generator.generate();
            }
        } finally {
            if (input != null)
                input.close();
        }
    }
}
