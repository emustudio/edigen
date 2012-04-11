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
import edigen.Specification;
import edigen.util.Template;

/**
 * The disassembler generator.
 * @author Matúš Sulír
 */
public class DisassemblerGenerator extends Generator {

    private Specification specification;

    /**
     * Constructs the disassembler generator.
     * @param specification the specification node
     * @param className the name of the resulting class
     */
    public DisassemblerGenerator(Specification specification, String className) {
        super("/edigen/res/Disassembler.egt", className);
        this.specification = specification;
    }
    
    /**
     * Transforms the AST to the form suitable for code generation.
     * @throws SemanticException when a semantic error occurs
     */
    @Override
    public void transform() throws SemanticException {
        
    }

    /**
     * Fills the template with variables and the generated code.
     * @param template the template object
     */
    @Override
    protected void fillTemplate(Template template) {
        super.fillTemplate(template);
    }
    
}
