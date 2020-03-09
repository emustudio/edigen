/*
 * Copyright (C) 2011, 2012 Matúš Sulír
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

import net.emustudio.edigen.parser.ParseException;
import net.emustudio.edigen.ui.Argument;
import net.emustudio.edigen.ui.CommandLine;
import net.emustudio.edigen.ui.CommandLineException;
import net.emustudio.edigen.ui.Help;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static net.emustudio.edigen.Setting.*;

/**
 * The main application class.
 *
 * @author Matúš Sulír
 */
public class Edigen {

    private static final Argument[] ARGUMENTS = {
        new Argument("<specification> - File containing the description of instructions",
            SPECIFICATION),
        new Argument("<decoder> - Resulting instruction decoder package + class name",
            DECODER_NAME),
        new Argument("<disassembler> - Resulting disassembler package + class name",
            DISASSEMBLER_NAME),
        new Argument("ao", "Write generated disassembler file to <directory>",
            DISASSEMBLER_DIRECTORY),
        new Argument("at", "Use <template> for disassembler instead of the default one",
                DISASSEMBLER_TEMPLATE),
        new Argument("d", "Enable debug mode", DEBUG),
        new Argument("do", "Write generated decoder file to <directory>", DECODER_DIRECTORY),
        new Argument("dt", "Use <template> for decoder instead of the default one",
                DECODER_TEMPLATE)
    };

    /**
     * The application entry point used when running the program from the
     * command line.
     * 
     * Displays information or error messages and calls the translator.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean success = false;
        
        try {
            new Edigen().run(args);
            success = true;
        } catch (CommandLineException ex) {
            if (args.length == 0)
                success = true;
            else
                System.out.println("\nError: " + ex.getMessage() + ".\n");
            
            Help help = new Help("java -jar edigen.jar", new CommandLine(ARGUMENTS));
            System.out.print(help.generate());
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open file: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error during file manipulation: " + ex.getMessage());
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        } catch (SemanticException ex) {
            System.out.println("Error: " + ex.getMessage() + ".");
        } finally {
            if (!success)
                System.exit(1);
        }
    }
    
    /**
     * Runs the generator without calling System.exit() - useful when running
     * from a Maven plugin.
     * @param args the arguments
     * @throws CommandLineException when the arguments are invalid
     * @throws IOException when an I/O error occurs
     * @throws ParseException when the input file can not be parsed
     * @throws SemanticException when the input file is semantically invalid
     */
    public void run(String[] args) throws CommandLineException, IOException,
            ParseException, SemanticException {
        System.out.println("Edigen - Emulator Disassembler Generator");
        
        CommandLine commandLine = new CommandLine(ARGUMENTS);
        Map<Setting, String> configuration = commandLine.parse(args);
        new Translator(configuration).translate();
        
        System.out.println("Instruction decoder and disassembler successfully generated.");
    }
}
