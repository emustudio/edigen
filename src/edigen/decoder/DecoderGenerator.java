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
package edigen.decoder;

import edigen.SemanticException;
import edigen.decoder.tree.Decoder;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.util.Template;
import java.io.*;
import java.util.Map;

/**
 * The instruction decoder generator.
 *
 * @author Matúš Sulír
 */
public class DecoderGenerator {

    private Map<String, String> settings;
    private boolean debug = false;
    private PrintStream debugStream = null;
    private Decoder decoder;

    /**
     * Constructs the generator.
     * @param settings the configuration obtained e.g. from the command line
     */
    public DecoderGenerator(Map<String, String> settings) {
        this.settings = settings;
        
        if (settings.containsKey("debug")) {
            debug = true;
            this.debugStream = System.out;
        }
    }
    
    /**
     * Parses the specification, transforms the tree and generates the code.
     * @throws IOException when the file can not be read / written
     * @throws ParseException when the input file can not be parsed
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void generate() throws IOException, ParseException, SemanticException {
        BufferedReader specification = null, templateSource = null;
        BufferedWriter output = null;
        
        try {
            specification = new BufferedReader(new FileReader(settings.get("specification")));
            parse(specification);
            transform();
            
            templateSource = getTemplate();
            output = getOutput();
            
            Template template = new Template(templateSource, output);
            setTemplateVariables(template);
            template.write();
        } finally {
            closeAll(specification, templateSource, output);
        }
    }

    /**
     * Constructs a tree from the instruction decoder specification.
     * @param specification an open instruction decoder specification reader
     * @throws ParseException when a parse error occurs
     * @throws SemanticException when a semantic error occurs
     */
    private void parse(Reader specification) throws ParseException, SemanticException {
        // input file -> parse tree
        Parser p = new Parser(specification);
        decoder = p.parse();

        if (debug)
            decoder.dump(debugStream);
    }
    
    /**
     * Executes all tree transformations and checks.
     * @throws SemanticException when a semantic error occurs
     */
    private void transform() throws SemanticException {
        Visitor[] transforms = {
            new ResolveNamesVisitor(),
            new JoinVisitor(),
            new SplitVisitor(),
            new MoveVariantVisitor(),
            new GroupVisitor(),
            new DetectAmbiguousVisitor(),
            new MoveMaskVisitor(),
            new RemovePatternVisitor()
        };

        for (Visitor visitor : transforms) {
            decoder.accept(visitor);

            if (debug)
                decoder.dump(debugStream);
        }
    }
    
    /**
     * Returns the open reader of the template file.
     * @return the template reader
     * @throws FileNotFoundException when the file can not be open
     */
    private BufferedReader getTemplate() throws FileNotFoundException {
        BufferedReader templateSource;
        
        if (settings.containsKey("decoder_template")) {
            String file = settings.get("decoder_template");
            templateSource = new BufferedReader(new FileReader(file));
        } else {
            InputStream stream = getClass().getResourceAsStream("/edigen/res/Decoder.egt");
            templateSource = new BufferedReader(new InputStreamReader(stream));
        }
        
        return templateSource;
    }
    
    /**
     * Returns the open writer of the output file.
     * @return the file writer
     * @throws IOException when the file can not be open for writing
     */
    private BufferedWriter getOutput() throws IOException {
        String outputFile = settings.get("decoder_class") + ".java";
        File outputPath;

        if (settings.containsKey("output_dir"))
            outputPath = new File(settings.get("output_dir"), outputFile);
        else
            outputPath = new File(outputFile);
        
        return new BufferedWriter(new FileWriter(outputPath));
    }
    
    /**
     * Sets the variables used in the template file.
     * @param template the template object
     * @throws SemanticException never
     */
    private void setTemplateVariables(Template template) throws SemanticException {
        template.setVariable("auto_gen_warning",
                "/* Auto-generated file. Do not modify. */");
        
        if (settings.containsKey("package"))
            template.setVariable("package_spec", "package " + settings.get("package") + ";");
        else
            template.setVariable("package_spec", "");
        
        template.setVariable("decoder_class", settings.get("decoder_class"));
        template.setVariable("root_rule", decoder.getRootRule().getMethodName());
        
        Writer fields = new StringWriter();
        decoder.accept(new GenerateFieldsVisitor(fields));
        template.setVariable("decoder_fields", fields.toString());
        
        Writer methods = new StringWriter();
        decoder.accept(new GenerateMethodsVisitor(methods));
        template.setVariable("decoder_methods", methods.toString());
    }
    
    /**
     * Closes all supplied streams.
     * @param streams the streams to close
     */
    private void closeAll(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException ex) { }
        }
    }
    
}
