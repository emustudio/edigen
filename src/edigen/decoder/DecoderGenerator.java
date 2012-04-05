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

import edigen.ConvertPass;
import edigen.NamePass;
import edigen.SemanticException;
import edigen.decoder.tree.Decoder;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.tree.SimpleNode;
import edigen.util.Template;
import edigen.util.TreePrinter;
import java.io.*;

/**
 * The instruction decoder generator.
 *
 * @author Matúš Sulír
 */
public class DecoderGenerator {

    private String specificationFile;
    private String className;
    private String templateFile;
    private boolean debug = false;
    private PrintStream debugStream = null;
    private Decoder decoder;

    /**
     * Constructs the generator.
     * @param specificationFile the specification file name
     * @param className the instruction decoder output class name
     * @param templateFile the instruction decoder template file name
     */
    public DecoderGenerator(String specificationFile, String className, String templateFile) {
        this.specificationFile = specificationFile;
        this.className = className;
        this.templateFile = templateFile;
    }

    /**
     * Enables dumping of the tree after each pass / transformation.
     * 
     * @param debugStream the debugging output stream
     */
    public void enableDebugging(PrintStream debugStream) {
        debug = true;
        this.debugStream = debugStream;
    }

    /**
     * Disables dumping of the tree.
     * @see #enableDebugging(java.io.PrintStream)
     */
    public void disableDebugging() {
        debug = false;
    }
    
    /**
     * Parses the specification, transforms the tree and generates the code.
     */
    public void generate() {
        BufferedReader specification = null, templateSource = null;
        BufferedWriter output = null;
        
        try {
            specification = new BufferedReader(new FileReader(specificationFile));
            parse(specification);
            transform();
            
            if (templateFile == null) {
                InputStream stream = getClass().getResourceAsStream("/edigen/res/Decoder.egt");
                templateSource = new BufferedReader(new InputStreamReader(stream));
            } else {
                templateSource = new BufferedReader(new FileReader(templateFile));
            }
            
            output = new BufferedWriter(new FileWriter(className + ".java"));
            
            Template template = new Template(templateSource, output);
            setTemplateVariables(template);
            template.write();
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open file: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error during file manipulation: " + ex.getMessage());
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        } catch (SemanticException ex) {
            System.out.println("Error: " + ex.getMessage() + ".");
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
        SimpleNode rootNode = p.parse();

        if (debug)
            new TreePrinter(debugStream).dump(rootNode);

        // parse tree -> customized tree containing only rules fo far
        decoder = new Decoder();
        NamePass namePass = new NamePass(decoder);
        namePass.checkNode(rootNode);

        if (debug)
            decoder.dump(debugStream);

        // parse tree -> fully-populated customized tree
        ConvertPass converter = new ConvertPass(decoder);
        rootNode.jjtAccept(converter, null);

        if (debug)
            decoder.dump(debugStream);
    }
    
    /**
     * Executes all tree transformations and checks.
     * @throws SemanticException when a semantic error occurs
     */
    private void transform() throws SemanticException {
        Visitor[] transforms = {
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
     * Sets the variables used in the template file.
     * @param template the template object
     * @throws SemanticException never
     */
    private void setTemplateVariables(Template template) throws SemanticException {
        template.setVariable("decoder_class", className);
        template.setVariable("root_rule", decoder.getRootRule().getName());
        
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
