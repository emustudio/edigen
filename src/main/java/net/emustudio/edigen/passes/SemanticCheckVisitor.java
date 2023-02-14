/*
 * This file is part of edigen.
 *
 * Copyright (C) 2011-2023 Matúš Sulír, Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A visitor which checks for additional semantic errors (not directly related to name resolution).
 */
public class SemanticCheckVisitor extends Visitor {

    private final Set<Set<String>> formatSet = new HashSet<>();
    private final Set<String> valueSet = new HashSet<>();
    private boolean variantReturns;
    private final Set<String> returningRules = new HashSet<>();
    private Subrule subruleWithoutLength;

    /**
     * Adds the rule to the set of returning rules if one of its variants
     * return.
     * @param rule the rule node
     * @throws SemanticException on semantic error
     */
    @Override
    public void visit(Rule rule) throws SemanticException {
        variantReturns = false;
        rule.acceptChildren(this);

        if (variantReturns) {
            returningRules.addAll(rule.getNames());
        }
    }

    /**
     * Sets the flag if the variant returns something and starts checking for
     * subrule errors.
     *
     * The subrule without length must occur only at the end of the variant.
     * @param variant the variant node
     * @throws SemanticException on subrule error
     */
    @Override
    public void visit(Variant variant) throws SemanticException {
        if (variant.returns())
            variantReturns = true;

        subruleWithoutLength = null;
        variant.acceptChildren(this);
    }

    /**
     * Checks whether a subrule without length was already defined in this
     * variant.
     * @param pattern the pattern node
     * @throws SemanticException if a subrule without length was defined
     */
    @Override
    public void visit(Pattern pattern) throws SemanticException {
        checkSubruleWithoutLength();
    }

    /**
     * Checks whether a subrule without length was already defined in this
     * variant and sets the flag if this subrule does not have a specified
     * length.
     * @param subrule the surule node
     * @throws SemanticException if a subrule without length was defined
     */
    @Override
    public void visit(Subrule subrule) throws SemanticException {
        checkSubruleWithoutLength();

        if (subrule.getLength() == null)
            subruleWithoutLength = subrule;
    }

    /**
     * Finds out whether the particular set of rules was not already used.
     *
     * Otherwise it would be ambiguous which format to apply when the decoded
     * instruction contained this set of rules.
     * @param format the format node
     * @throws SemanticException if the set of values was used in multiple
     *         formats
     */
    @Override
    public void visit(Format format) throws SemanticException {
        valueSet.clear();
        format.acceptChildren(this);

        if (!formatSet.contains(valueSet)) {
            formatSet.add(valueSet);
        } else {
            StringBuilder values = new StringBuilder();

            for (String value : valueSet) {
                if (values.length() != 0)
                    values.append(", ");

                values.append(value);
            }

            throw new SemanticException("Set of values \"" + values
                    + "\" is contained in multiple disassembler formats", format);
        }
    }

    /**
     * Finds out whether at least one rule's variant can return a value.
     *
     * Probably only the fields for variants returning a value will be present
     * in the generated code, so using other variants in a disassembler would
     * cause a syntax error.
     * @param value the value node
     * @throws SemanticException if the rule which this value refers to
     *         never returns a value
     */
    @Override
    public void visit(Value value) throws SemanticException {
        if (returningRules.contains(value.getName())) {
            valueSet.add(value.getName());
        } else {
            throw new SemanticException("Rule \"" + value.getName() + "\" never"
                    + " returns a value, but is used in the disassembler", value);
        }
    }

    /**
     * Checks whether a subrule without length was already defined in this
     * variant.
     * @throws SemanticException if a subrule without length was already defined
     */
    private void checkSubruleWithoutLength() throws SemanticException {
        if (subruleWithoutLength != null) {
            String name = subruleWithoutLength.getName();
            String message = "Subrule \"" + name + "\" does not have"
            + " a specified length and is not contained at the variant end";

            throw new SemanticException(message, subruleWithoutLength);
        }
    }
}
