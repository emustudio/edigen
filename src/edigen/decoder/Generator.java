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
import edigen.util.TreePrinter;
import java.io.PrintStream;
import java.io.Reader;

/**
 * The instruction decoder generator.
 *
 * @author Matúš Sulír
 */
public class Generator {

    private Reader input;
    private PrintStream output;
    private String outputClass;
    private boolean debug = false;
    private PrintStream debugStream = null;
    private Decoder decoder;

    /**
     * Constructs the generator.
     *
     * @param inputFile the input file name
     */
    public Generator(Reader input, PrintStream output, String outputClass) {
        this.input = input;
        this.output = output;
        this.outputClass = outputClass;
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
     * Parses the input file, creates a new tree from the parse tree and
     * transforms it.
     */
    public void run() {
        try {
            // input file -> parse tree
            Parser p = new Parser(input);
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

            // apply transformations
            transform();
            
            // generate code
            decoder.accept(new GenerateCodeVisitor(output, outputClass));
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        } catch (SemanticException ex) {
            System.out.println("Error: " + ex.getMessage() + ".");
        }
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
            new MoveMaskVisitor()
        };

        for (Visitor visitor : transforms) {
            decoder.accept(visitor);

            if (debug)
                decoder.dump(debugStream);
        }
    }
}
