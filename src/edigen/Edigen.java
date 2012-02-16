/*
 * Copyright (C) 2011 Matúš Sulír
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

import edigen.decoder.JoinVisitor;
import edigen.decoder.MoveVariantVisitor;
import edigen.decoder.SplitVisitor;
import edigen.decoder.tree.Decoder;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.tree.SimpleNode;
import edigen.util.TreePrinter;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The main application class.
 * @author Matúš Sulír
 */
public class Edigen {

    /**
     * The application entry point.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Edigen edigen = new Edigen();
        
        if (args.length == 1) {
            edigen.runGenerator(args[0]);
        } else {
            System.out.println("Usage: edigen.jar filename");
        }
    }
    
    /**
     * Runs the disassembler generator (currently only partially implemented).
     * @param inputFile the input file name
     */
    private void runGenerator(String inputFile) {
        try {
            Parser p = new Parser(new FileReader(inputFile));

            try {
                // input file -> parse tree
                SimpleNode rootNode = p.parse();
                new TreePrinter(System.out).dump(rootNode);

                Decoder decoder = new Decoder();
                
                // parse tree -> customized tree containing only rules fo far
                NamePass namePass = new NamePass(decoder);
                namePass.checkNode(rootNode);
                decoder.dump(System.out);
                
                // parse tree -> fully-populated customized tree
                ConvertPass converter = new ConvertPass(decoder);
                rootNode.jjtAccept(converter, null);
                decoder.dump(System.out);
                
                // transformation: join
                decoder.accept(new JoinVisitor());
                decoder.dump(System.out);
                
                // transformation: split
                decoder.accept(new SplitVisitor());
                decoder.dump(System.out);
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (SemanticException ex) {
                System.out.println("Error: " + ex.getMessage() + ".");
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open input file.");
        }
    }
}
