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
package edigen.disasm;

import edigen.Generator;
import edigen.SemanticException;
import edigen.tree.Disassembler;
import edigen.tree.Specification;
import edigen.util.Template;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The disassembler generator.
 * @author Matúš Sulír
 */
public class DisassemblerGenerator extends Generator {

    private Disassembler disassembler;
    private String disassemblerClass;
    private String decoderClass;
    private boolean bigEndian = false;

    /**
     * Constructs the disassembler generator.
     * @param specification the specification node
     * @param disassemblerClass the name of the resulting class
     * @param decoderClass the name of the associated decoder class
     */
    public DisassemblerGenerator(Specification specification,
            String disassemblerClass, String decoderClass) {
        super("/Disassembler.egt", disassemblerClass);
        
        this.disassembler = specification.getDisassembler();
        this.disassemblerClass = disassemblerClass;
        this.decoderClass = decoderClass;
    }
    
    /**
     * Transforms the AST and/or checks for semantic errors.
     * @throws SemanticException when a semantic error occurs
     */
    @Override
    public void transform() throws SemanticException {
        disassembler.accept(new SemanticCheckVisitor());
    }

    /**
     * Sets the disassembler data endianness (default is little).
     * @param bigEndian true for big endian, false for little endian
     */
    public void setEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }
    
    /**
     * Fills the template with variables and the generated code.
     * @param template the template object
     * @throws SemanticException never
     */
    @Override
    protected void fillTemplate(Template template) throws SemanticException {
        super.fillTemplate(template);
        
        template.setVariable("disasm_class", disassemblerClass);
        template.setVariable("decoder_class", decoderClass);
        template.setVariable("little_endian", bigEndian ? "false" : "true");
        
        Writer formats = new StringWriter();
        disassembler.accept(new GenerateFormatsVisitor(formats));
        template.setVariable("disasm_formats", formats.toString());

        Writer values = new StringWriter();
        disassembler.accept(new GenerateValuesVisitor(values));
        template.setVariable("disasm_values", values.toString());
    }
    
}
