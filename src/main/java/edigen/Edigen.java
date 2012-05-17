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

import static edigen.Setting.*;
import edigen.parser.ParseException;
import edigen.ui.Argument;
import edigen.ui.CommandLine;
import edigen.ui.CommandLineException;
import edigen.ui.Help;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * The main application class.
 *
 * @author Matúš Sulír
 */
public class Edigen {

    private static final Argument[] ARGUMENTS = {
        new Argument("<specification> - File containing the description of instructions",
            SPECIFICATION),
        new Argument("<decoder_class> - Resulting instruction decoder class name",
            DECODER_CLASS),
        new Argument("<disassembler_class> - Resulting disassembler class name",
            DISASSEMBLER_CLASS),
        new Argument("at", "Use <template> for disassembler instead of the default one",
                DISASSEMBLER_TEMPLATE),
        new Argument("d", "Enable debug mode", DEBUG),
        new Argument("dt", "Use <template> for instruction decoder instead of the default one",
                DECODER_TEMPLATE),
        new Argument("o", "Write generated files to <directory>", OUTPUT_DIRECTORY),
        new Argument("p", "Make generated classes members of <package>.impl and <package>.gui", PACKAGE)
    };

    /**
     * The application entry point.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Edigen - Emulator Disassembler Generator");
        CommandLine commandLine = new CommandLine(ARGUMENTS);
        String help = new Help("java -jar edigen.jar", commandLine).generate();
        boolean success = false;
        
        try {
            Map<Setting, String> configuration = commandLine.parse(args);
            
            Translator generator = new Translator(configuration);
            generator.translate();
            System.out.println("Decoder and disassembler successfully generated.");
            success = true;
        } catch (CommandLineException ex) {
            if (args.length == 0)
                success = true;
            else
                System.out.println("\nError: " + ex.getMessage() + ".\n");
            
            System.out.print(help);
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
}
