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

import edigen.decoder.DecoderGenerator;

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
        if (args.length == 2 || args.length == 3) {
            String specificationFile = args[0];
            String decoderClass = args[1];
            String decoderTemplate = (args.length == 3) ? args[2] : null;
            
            DecoderGenerator decoder = new DecoderGenerator(specificationFile,
                    decoderClass, decoderTemplate);
            decoder.enableDebugging(System.out);
            decoder.generate();
        } else {
            System.out.println("Usage: edigen.jar SpecificationFile DecoderClass [DecoderTemplate]");
        }
    }
}
