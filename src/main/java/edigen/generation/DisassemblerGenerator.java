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
import edigen.nodes.Disassembler;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The disassembler generator.
 * @author Matúš Sulír
 */
public class DisassemblerGenerator extends Generator {

    private final Disassembler disassembler;
    private final String decoderName;

    /**
     * Constructs the disassembler generator.
     * @param disassembler the disassembler node
     * @param disassemblerName the resulting package + class name
     * @param decoderName the package + class name of the decoder
     */
    public DisassemblerGenerator(Disassembler disassembler,
            String disassemblerName, String decoderName) {
        super("/Disassembler.edt", disassemblerName);
        
        this.disassembler = disassembler;
        this.decoderName = decoderName;
    }
    
    /**
     * Fills the template with variables and the generated code.
     * @param template the template object
     * @throws SemanticException never
     */
    @Override
    protected void fillTemplate(Template template) throws SemanticException {
        super.fillTemplate(template);
        
        template.setVariable("disasm_package", getPackageName());
        template.setVariable("disasm_class", getClassName());
        template.setVariable("decoder_name", decoderName);
        
        Writer formats = new StringWriter();
        disassembler.accept(new GenerateFormatsVisitor(formats));
        template.setVariable("disasm_formats", formats.toString());

        Writer values = new StringWriter();
        disassembler.accept(new GenerateParametersVisitor(values));
        template.setVariable("disasm_parameters", values.toString());
    }
    
}
