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

import edigen.SemanticException;
import edigen.decoder.tree.*;
import edigen.util.PrettyPrinter;
import java.io.PrintStream;

/**
 * A visitor which generates Java source code of the instruction decoder.
 * @author Matúš Sulír
 */
public class GenerateCodeVisitor extends Visitor {

    private PrettyPrinter output;
    private String outputClass;
    private String currentRule;
    private boolean isDefaultCase = false;
    
    /**
     * Constructs the visitor.
     * @param output the output stream to write the code to
     */
    public GenerateCodeVisitor(PrintStream output, String outputClass) {
        this.output = new PrettyPrinter(output);
        this.outputClass = outputClass;
    }

    /**
     * Writes the class skeleton.
     * @param decoder the decoder node
     * @throws SemanticException never
     */
    @Override
    public void visit(Decoder decoder) throws SemanticException {
        put("class " + outputClass + " {");
        put("private byte b;");
        put("private byte[] instruction = new byte[128];");
        put("private int start = 0;");
        put("private int length = 0;", true);
        decoder.acceptChildren(this);
        put("}");
    }
    
    /**
     * Writes the method definition.
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        currentRule = rule.getName();
        
        put("private void " + currentRule + "(int start) {");
        rule.acceptChildren(this);
        put("}", true);
    }

    /**
     * Writes the <code>switch</code> statement (if the mask is not zero-only).
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        if (!isDefaultCase)
            put("b = read();", true);
        
        isDefaultCase = false;
        
        if (mask.getBits().containsOnly(false)) {
            mask.acceptChildren(this);
        } else {
            put("switch (b & 0x" + mask.getBits().toHexadecimal() + ") {");
            mask.acceptChildren(this);
            put("}");
        }
    }

    /**
     * Writes the <code>case</code> / <code>default</code> statement.
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        isDefaultCase = (pattern.getBits().getLength() == 0);
        
        if (!isDefaultCase)
            put("case 0x" + pattern.getBits().toHexadecimal() + ":");
        else
            put("default:");
        
        pattern.acceptChildren(this);
        put("break;");
    }

    /**
     * Writes the method invocation.
     * @param subrule the subrule node
     */
    @Override
    public void visit(Subrule subrule) {
        put(subrule.getName() + "(start + " + subrule.getStart() + ");");
    }
    
    /**
     * Puts the line of source code into the prettifier, which writes it into
     * the output stream.
     * @param lineOfCode the line of source code
     * @param newBlock true if double newline should be printed after the
     *                 statement
     */
    private void put(String lineOfCode, boolean newBlock) {
        output.writeLine(lineOfCode);
        
        if (newBlock)
            output.writeLine("");
    }

    /**
     * Puts the line of source code into the prettifier, which writes it into
     * the output stream.
     * @param lineOfCode the line of source code
     */
    private void put(String lineOfCode) {
        put(lineOfCode, false);
    }
}
