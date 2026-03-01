/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Rule variant node.
 * <p>
 * One of the instruction decoder's task is to find out which variant of the
 * particular rule matches against the part of the decoded instruction.
 */
public class Variant extends TreeNode {

    /**
     * Constructs a new variant node.
     */
    public Variant() {
    }

    private enum ReturnType {
        NOTHING,
        STRING,
        SUBRULE
    }

    private static final Pattern LEADING_DIGITS = Pattern.compile("\\d.*");
    private static final Pattern NON_WORD = Pattern.compile("\\W");

    private ReturnType returnType = ReturnType.NOTHING;
    private String returnString;
    private Subrule returnSubrule;

    /**
     * Returns the string which this variant returns.
     *
     * @return the string, or null if the variant returns a subrule or nothing
     */
    public String getReturnString() {
        if (returnType == ReturnType.STRING)
            return returnString;
        else
            return null;
    }

    /**
     * Tells the variant to return the string on match.
     *
     * @param returnString the string to return
     */
    public void setReturnString(String returnString) {
        returnType = ReturnType.STRING;
        this.returnString = returnString;
    }

    /**
     * Returns the subrule which this variant returns.
     *
     * @return the subrule, or null if the variant returns a string or nothing
     */
    public Subrule getReturnSubrule() {
        if (returnType == ReturnType.SUBRULE)
            return returnSubrule;
        else
            return null;
    }

    /**
     * Tells the variant to return the value of the specified subrule.
     *
     * @param returnRule the subrule, must be contained in the pattern
     */
    public void setReturnSubrule(Subrule returnRule) {
        returnType = ReturnType.SUBRULE;
        this.returnSubrule = returnRule;
    }

    /**
     * Returns true if the variant returns a string or a subrule.
     *
     * @return true if the variant returns, false otherwise
     */
    public boolean returns() {
        return returnType != ReturnType.NOTHING;
    }

    /**
     * Returns the generated field name if the variant returns a string.
     *
     * @return the field name, or null if the variant does not return a string
     */
    public String getFieldName() {
        if (returnType == ReturnType.STRING)
            return makeIdentifierName(returnString);
        else
            return null;
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor the visitor object
     * @throws SemanticException depends on the specific visitor
     */
    @Override
    public void accept(Visitor visitor) throws SemanticException {
        visitor.visit(this);
    }

    /**
     * Returns the mask as a string in binary notation.
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Variant");

        if (returnType == ReturnType.STRING)
            result.append(": return \"").append(returnString).append('"');
        else if (returnType == ReturnType.SUBRULE)
            result.append(": return ").append(returnSubrule);

        return result.toString();
    }

    /**
     * Makes a valid Java identifier name from the string.
     *
     * @param string the string
     * @return the identifier name
     */
    private String makeIdentifierName(String string) {
        string = string.trim().toUpperCase();

        if (LEADING_DIGITS.matcher(string).matches())
            string = '_' + string;

        string = string.replace(' ', '_');

        return NON_WORD.matcher(string).replaceAll("_");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variant variant = (Variant) o;

        if (returnType != variant.returnType) return false;
        if (!Objects.equals(returnString, variant.returnString))
            return false;
        return Objects.equals(returnSubrule, variant.returnSubrule);
    }

    @Override
    public TreeNode shallowCopy() {
        Variant cp = new Variant();
        cp.returnType = returnType;
        cp.returnString = returnString;
        cp.returnSubrule = returnSubrule;
        return cp;
    }
}
