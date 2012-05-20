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
package edigen.generation;

import edigen.SemanticException;
import edigen.misc.Template;
import edigen.nodes.Decoder;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The instruction decoder generator.
 *
 * @author Matúš Sulír
 */
public class DecoderGenerator extends Generator {

    private Decoder decoder;
    private String className;

    /**
     * Constructs the instruction decoder generator.
     * @param decoder the decoder node
     * @param className the name of the resulting class
     */
    public DecoderGenerator(Decoder decoder, String className) {
        super("/Decoder.egt", className);

        this.decoder = decoder;
        this.className = className;
    }

    /**
     * Fills the template with variables and the generated code.
     * @param template the template object
     * @throws SemanticException never
     */
    @Override
    protected void fillTemplate(Template template) throws SemanticException {
        super.fillTemplate(template);
        
        template.setVariable("decoder_class", className);
        template.setVariable("root_rule", decoder.getRootRule().getMethodName());

        Writer fields = new StringWriter();
        decoder.accept(new GenerateFieldsVisitor(fields));
        template.setVariable("decoder_fields", fields.toString());

        Writer methods = new StringWriter();
        decoder.accept(new GenerateMethodsVisitor(methods));
        template.setVariable("decoder_methods", methods.toString());
    }
    
}
