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

import edigen.objects.Decoder;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.tree.SimpleNode;
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
                // lexical and syntactic analysis
                SimpleNode rootNode = p.parse();
                Decoder decoder = new Decoder();

                // the first semantic pass
                NamePass namePass = new NamePass(decoder);
                namePass.checkNode(rootNode);
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (SemanticException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open input file.");
        }
    }
}
