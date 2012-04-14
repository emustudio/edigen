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
package edigen.decoder;

import edigen.Generator;
import edigen.SemanticException;
import edigen.Visitor;
import edigen.tree.Decoder;
import edigen.tree.Specification;
import edigen.util.Template;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The instruction decoder generator.
 *
 * @author Matúš Sulír
 */
public class DecoderGenerator extends Generator {

    private Specification specification;
    private Decoder decoder;
    private String className;

    /**
     * Constructs the instruction decoder generator.
     * @param specification the specification node
     * @param className the name of the resulting class
     */
    public DecoderGenerator(Specification specification, String className) {
        super("/edigen/res/Decoder.egt", className);
        
        this.specification = specification;
        this.decoder = specification.getDecoder();
        this.className = className;
    }
    
    /**
     * Transforms the AST to the form suitable for code generation.
     * @throws SemanticException when a semantic error occurs
     */
    @Override
    public void transform() throws SemanticException {
        specification.accept(new ResolveNamesVisitor());
        
        Visitor[] transforms = {
            new JoinVisitor(),
            new SplitVisitor(),
            new MoveVariantVisitor(),
            new GroupVisitor(),
            new DetectAmbiguousVisitor(),
            new MoveMaskVisitor(),
            new RemovePatternVisitor()
        };

        for (Visitor visitor : transforms) {
            decoder.accept(visitor);
            dump(decoder);
        }
    }

    /**
     * Fills the template with variables and the generated code.
     * @param template the template object
     */
    @Override
    protected void fillTemplate(Template template) {
        super.fillTemplate(template);
        
        template.setVariable("decoder_class", className);
        template.setVariable("root_rule", decoder.getRootRule().getMethodName());
        
        try {
            Writer fields = new StringWriter();
            decoder.accept(new GenerateFieldsVisitor(fields));
            template.setVariable("decoder_fields", fields.toString());

            Writer methods = new StringWriter();
            decoder.accept(new GenerateMethodsVisitor(methods));
            template.setVariable("decoder_methods", methods.toString());
        } catch (SemanticException ex) {
            // code generation does not produce semantic errors
        }
    }
    
}
