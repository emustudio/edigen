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

import edigen.debug.PrintVisitor;
import edigen.parser.ParseException;
import edigen.parser.Parser;
import edigen.parser.TokenMgrError;
import edigen.tree.SimpleNode;
import java.io.StringReader;

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
        Parser p = new Parser(new StringReader((". %% .")));
        
        try {
            SimpleNode rootNode = p.parse();
            PrintVisitor printer = new PrintVisitor();
            rootNode.jjtAccept(printer, null);
        } catch (ParseException | TokenMgrError ex) {
            ex.printStackTrace();
        }
    }
}
