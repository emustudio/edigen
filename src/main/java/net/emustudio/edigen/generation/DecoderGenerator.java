/*
 * Copyright (C) 2011-2022 Matúš Sulír, Peter Jakubčo
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
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.misc.Template;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;

import java.io.StringWriter;
import java.io.Writer;

/**
 * The instruction decoder generator.
 *
 */
public class DecoderGenerator extends Generator {

    private final Decoder decoder;

    /**
     * Constructs the instruction decoder generator.
     * @param decoder the decoder node
     * @param name the resulting package + class name
     */
    public DecoderGenerator(Decoder decoder, String name) {
        super("/Decoder.edt", name);

        this.decoder = decoder;
    }

    /**
     * Fills the template with variables and the generated code.
     * @param template the template object
     * @throws SemanticException never
     */
    @Override
    protected void fillTemplate(Template template) throws SemanticException {
        super.fillTemplate(template);

        template.setVariable("decoder_package", getPackageName());
        template.setVariable("decoder_class", getClassName());

        Rule rootRule = decoder.getRootRule();
        if (rootRule.hasOnlyOneName()) {
            template.setVariable("root_rule", rootRule.getMethodName() + "(0)");
        } else {
            template.setVariable("root_rule", rootRule.getMethodName() + "(0, " + rootRule.getFieldName() + ")");
        }

        Writer fields = new StringWriter();
        decoder.accept(new GenerateFieldsVisitor(fields));
        template.setVariable("decoder_fields", fields.toString());

        Writer methods = new StringWriter();
        decoder.accept(new GenerateMethodsVisitor(methods));
        template.setVariable("decoder_methods", methods.toString());

        Writer maxInstructionBytes = new StringWriter();
        decoder.accept(new GenerateMaxInstructionBytes(maxInstructionBytes));
        template.setVariable("max_instruction_bytes", maxInstructionBytes.toString());
    }
    
}
