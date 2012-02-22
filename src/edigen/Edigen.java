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

import edigen.decoder.Generator;
import java.io.*;

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
        if (args.length == 2) {
            String inputFile = args[0];
            String outputClass = args[1];

            new Edigen().generateDecoder(inputFile, outputClass);
        } else {
            System.out.println("Usage: edigen.jar InputFile OutputClassName");
        }
    }
    
    /**
     * Generates the instruction decoder.
     * @param inputFile the input file name
     * @param outputClass the output class name (whithout the ".java" extension)
     */
    public void generateDecoder(String inputFile, String outputClass) {
        Reader input = null;
        PrintStream output = null;
        
        try {
            input = new FileReader(inputFile);
            output = new PrintStream(outputClass + ".java");

            Generator generator = new Generator(input, output, outputClass);
            generator.enableDebugging(System.out);
            generator.run();
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open file: " + ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) { }
            }

            if (output != null)
                output.close();
        }
    }
}
