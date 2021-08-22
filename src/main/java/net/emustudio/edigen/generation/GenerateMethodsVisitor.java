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
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.misc.PrettyPrinter;
import net.emustudio.edigen.nodes.*;

import java.io.Writer;
import java.util.*;

import static net.emustudio.edigen.nodes.Decoder.UNIT_SIZE_BITS;
import static net.emustudio.edigen.nodes.Decoder.UNIT_SIZE_BYTES;

/**
 * A visitor which generates Java source code of the instruction decoder methods
 * for all rules.
 */
public class GenerateMethodsVisitor extends Visitor {

    private final PrettyPrinter printer;
    private final Queue<Rule> rootRulesLeft = new LinkedList<>();
    private Rule ruleToTry;
    private Rule currentRule;
    private boolean isDefaultCase = false;

    private boolean unitWasRead;
    private int unitLastStart;
    private int unitLastLength;

    /**
     * Constructs the visitor.
     * @param output the output stream to write the code to
     */
    public GenerateMethodsVisitor(Writer output) {
        this.printer = new PrettyPrinter(output);
    }

    /**
     * Finds out which root rules are available.
     *
     * @param decoder decoder node
     * @throws SemanticException never
     */
    @Override
    public void visit(Decoder decoder) throws SemanticException {
        List<Rule> rootRulesToTry = new ArrayList<>(decoder.getRootRules());
        rootRulesToTry.remove(0);
        rootRulesLeft.addAll(rootRulesToTry);
        decoder.acceptChildren(this);
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
        unitWasRead = false;

        if (rule.isRoot() && !rootRulesLeft.isEmpty()) {
            ruleToTry = rootRulesLeft.poll();
        } else {
            ruleToTry = null;
        }

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
        int maskStart = mask.getStart();
        int maskLength = mask.getBits().getLength();
        boolean alreadyRead = unitWasRead && unitLastStart == maskStart && unitLastLength == maskLength;

        if (!isDefaultCase && !isZero && !alreadyRead) {
            if (maskLength > UNIT_SIZE_BITS) {
                throw new SemanticException(
                        String.format("Mask length %d is over maximum %d bits", maskLength, UNIT_SIZE_BITS),
                        mask
                );
            }
            if (maskStart == 0) {
                put(String.format("unit = readBits(start, %d);", maskLength), true);
            } else {
                put(String.format("unit = readBits(start + %d, %d);", maskStart, maskLength), true);
            }
            unitWasRead = true;
            unitLastStart = maskStart;
            unitLastLength = maskLength;
        }
        
        isDefaultCase = false;
        
        if (!isZero)
            put("switch (unit & 0x" + mask.getBits().toHexadecimal() + ") {");
        
        mask.acceptChildren(this);
        
        if (!isZero && !isDefaultCase) {
            put("default:");
            if (ruleToTry != null) {
                if (ruleToTry.hasOnlyOneName()) {
                    put(ruleToTry.getMethodName() + "(0);");
                } else {
                    put(ruleToTry.getMethodName() + "(0, " + ruleToTry.getFieldName() + ");");
                }
            } else {
                put("throw new InvalidInstructionException();");
            }
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
        boolean thisIsDefaultCase = pattern.getBits().getLength() == 0;
        if (!thisIsDefaultCase)
            put("case 0x" + pattern.getBits().toHexadecimal() + ":");
        else
            put("default:");
        
        pattern.acceptChildren(this);
        if (!thisIsDefaultCase)
            put("break;");

        isDefaultCase = thisIsDefaultCase;
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
                value = '"' + variant.getReturnString() + "\", " + variant.getFieldName();
                put(String.format("instruction.add(%s, %s);", field, value));
            } else {
                int start = variant.getReturnSubrule().getStart();
                int length = variant.getReturnSubrule().getLength();
                if (length > UNIT_SIZE_BITS) {
                    throw new SemanticException(
                            String.format(
                                    "Sub-rule %s length %d is over maximum %d bits",
                                    variant.getReturnSubrule().getName(), length, UNIT_SIZE_BITS),
                            variant
                    );
                }

                if (start == 0) {
                    value = String.format("readBits(start, %d)", length);
                } else {
                    value = String.format("readBits(start + %d, %d)", start, length);
                }
                put(String.format("instruction.add(%s, %s, %d);", field, value, length));
            }
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

        String methodName = subrule.getRule().getMethodName();
        int start = subrule.getStart();
        if (start == 0) {
            put(methodName + "(start" + fieldToWrite + ");");
        } else {
            put(methodName + "(start + " + subrule.getStart() + fieldToWrite + ");");
        }
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
