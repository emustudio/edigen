/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.nodes.*;

/**
 * Generic tree node visitor.
 * <p>
 * The subclasses can override needed methods to implement the expected
 * behavior when visiting the particular node. Non-overridden methods will have
 * the default behavior, which is to accept all children.
 */
public abstract class Visitor {

    /**
     * Constructs a new visitor.
     */
    public Visitor() {
    }

    /**
     * Visits a generic tree node.
     *
     * @param node the tree node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(TreeNode node) throws SemanticException {
        node.acceptChildren(this);
    }

    /**
     * Visits a decoder node.
     *
     * @param decoder the decoder node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);
    }

    /**
     * Visits a disassembler node.
     *
     * @param disassembler the disassembler node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Disassembler disassembler) throws SemanticException {
        disassembler.acceptChildren(this);
    }

    /**
     * Visits a format node.
     *
     * @param format the format node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Format format) throws SemanticException {
        format.acceptChildren(this);
    }

    /**
     * Visits a mask node.
     *
     * @param mask the mask node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Mask mask) throws SemanticException {
        mask.acceptChildren(this);
    }

    /**
     * Visits a pattern node.
     *
     * @param pattern the pattern node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Pattern pattern) throws SemanticException {
        pattern.acceptChildren(this);
    }

    /**
     * Visits a rule node.
     *
     * @param rule the rule node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Rule rule) throws SemanticException {
        rule.acceptChildren(this);
    }

    /**
     * Visits a specification node.
     *
     * @param specification the specification node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Specification specification) throws SemanticException {
        specification.acceptChildren(this);
    }

    /**
     * Visits a subrule node.
     *
     * @param subrule the subrule node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Subrule subrule) throws SemanticException {
        subrule.acceptChildren(this);
    }

    /**
     * Visits a value node.
     *
     * @param value the value node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Value value) throws SemanticException {
        value.acceptChildren(this);
    }

    /**
     * Visits a variant node.
     *
     * @param variant the variant node
     * @throws SemanticException depends on the specific visitor
     */
    public void visit(Variant variant) throws SemanticException {
        variant.acceptChildren(this);
    }
}
