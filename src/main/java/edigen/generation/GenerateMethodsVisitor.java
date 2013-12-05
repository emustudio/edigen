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
import edigen.Visitor;
import edigen.misc.PrettyPrinter;
import edigen.nodes.*;
import java.io.Writer;

/**
 * A visitor which generates Java source code of the instruction decoder methods
 * for all rules.
 * @author Matúš Sulír
 */
public class GenerateMethodsVisitor extends Visitor {

    private final PrettyPrinter printer;
    private Rule currentRule;
    private boolean isDefaultCase = false;
    
    /**
     * Constructs the visitor.
     * @param output the output stream to write the code to
     */
    public GenerateMethodsVisitor(Writer output) {
        this.printer = new PrettyPrinter(output);
    }
    
    /**
     * Writes the method definition.
     * @param rule the rule node
     * @throws SemanticException never
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        currentRule = rule;
        isDefaultCase = false;
        
        String secondParameter = rule.hasOnlyOneName() ? "" : ", int rule";
        
        put("private void " + currentRule.getMethodName() + "(int start"
                + secondParameter + ") throws InvalidInstructionException {");
        rule.acceptChildren(this);
        put("}", true);
    }

    /**
     * Writes the unit reading code and if the mask is not zero-only, also
     * writes the <code>switch</code> statement.
     * @param mask the mask node
     * @throws SemanticException never
     */
    @Override
    public void visit(Mask mask) throws SemanticException {
        boolean isZero = mask.getBits().containsOnly(false);
        
        if (!isDefaultCase) {
            put("unit = read(start + " + mask.getStart()
                    + ", " + mask.getBits().getLength() + ");", true);
        }
        
        isDefaultCase = false;
        
        if (!isZero)
            put("switch (unit & 0x" + mask.getBits().toHexadecimal() + ") {");
        
        mask.acceptChildren(this);
        
        if (!isZero && !isDefaultCase) {
            put("default:");
            put("throw new InvalidInstructionException();");
        }
        
        if (!isZero)
            put("}");
    }

    /**
     * Writes the <code>case</code> / <code>default</code> statement.
     * @param pattern the pattern node
     * @throws SemanticException never
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        if (pattern.getBits().getLength() != 0)
            put("case 0x" + pattern.getBits().toHexadecimal() + ":");
        else
            put("default:");
        
        pattern.acceptChildren(this);
        put("break;");
        isDefaultCase = (pattern.getBits().getLength() == 0);
    }

    /**
     * Writes the code for the recognized variant.
     * @param variant the variant node
     * @throws SemanticException never
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        if (variant.returns()) {
            String field = "rule";
            String value;
            
            if (currentRule.hasOnlyOneName())
                field = currentRule.getFieldName(currentRule.getNames().get(0));

            if (variant.getReturnString() != null) {
                value = '"' + variant.getReturnString() + "\", "
                        + variant.getFieldName();
            } else {
                int start = variant.getReturnSubrule().getStart();
                int length = variant.getReturnSubrule().getLength();
                
                value = String.format("getValue(start + %d, %d)", start, length);
            }
            
            put(String.format("instruction.add(%s, %s);", field, value));
        }
        
        variant.acceptChildren(this);
    }

    /**
     * Writes the method invocation.
     * 
     * If the rule has multiple names, one method is associated with multiple
     * rule names. So the particular field (rule name) must be passed as an
     * argument.
     * @param subrule the subrule node
     */
    @Override
    public void visit(Subrule subrule) {
        String fieldToWrite = "";
        
        if (!subrule.getRule().hasOnlyOneName())
            fieldToWrite = ", " + subrule.getFieldName();
        
        put(subrule.getRule().getMethodName() + "(start + " + subrule.getStart()
                + fieldToWrite + ");");
    }
    
    /**
     * Puts the line of source code into the prettifier, which writes it into
     * the output stream.
     * @param lineOfCode the line of source code
     * @param newBlock true if double newline should be printed after the
     *                 statement
     */
    private void put(String lineOfCode, boolean newBlock) {
        printer.writeLine(lineOfCode);
        
        if (newBlock)
            printer.writeLine("");
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
