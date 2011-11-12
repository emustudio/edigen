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

import edigen.debug.TreePrinter;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.tree.SimpleNode;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The main application class.
 * @author Matúš Sulír
 */
public class Main {

    /**
     * The application entry point.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                Parser p = new Parser(new FileReader(args[0]));

                try {
                    SimpleNode rootNode = p.parse();
                    TreePrinter printer = new TreePrinter();
                    printer.dump(rootNode);
                } catch (ParseException ex) {
                    System.out.println(ex.getMessage());
                }
            } catch (FileNotFoundException ex) {
                System.out.println("Could not open input file.");
            }
        } else {
            System.out.println("Usage: edigen.jar filename");
        }
    }
}
