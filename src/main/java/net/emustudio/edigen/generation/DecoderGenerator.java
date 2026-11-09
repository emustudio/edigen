/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.GenerationLog;
import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.misc.Template;
import net.emustudio.edigen.nodes.Decoder;
import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.Specification;

import java.io.StringWriter;
import java.io.Writer;

/**
 * The instruction decoder generator.
 *
 */
public class DecoderGenerator extends Generator {

    private final Decoder decoder;
    private final Specification specification;

    /**
     * Constructs the instruction decoder generator.
     *
     * @param specification the decoder rules and presentation formats
     * @param name    the resulting package + class name
     * @param log     receives generation messages
     */
    public DecoderGenerator(Specification specification, String name, GenerationLog log) {
        super("decoder", "/Decoder.edt", name, log);

        this.specification = specification;
        this.decoder = specification.getDecoder();
    }

    /**
     * Fills the template with variables and the generated code.
     *
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

        Writer formats = new StringWriter();
        specification.getDisassembler().accept(new GenerateFormatsVisitor(formats));
        template.setVariable("disasm_formats", formats.toString());
        Writer parameters = new StringWriter();
        specification.getDisassembler().accept(new GenerateParametersVisitor(parameters));
        template.setVariable("disasm_parameters", parameters.toString());
    }

}
