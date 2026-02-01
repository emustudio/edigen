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
    public void visit(TreeNode node) throws SemanticException {
        node.acceptChildren(this);
    }

    public void visit(Decoder decoder) throws SemanticException {
        decoder.acceptChildren(this);
    }

    public void visit(Disassembler disassembler) throws SemanticException {
        disassembler.acceptChildren(this);
    }

    public void visit(Format format) throws SemanticException {
        format.acceptChildren(this);
    }

    public void visit(Mask mask) throws SemanticException {
        mask.acceptChildren(this);
    }

    public void visit(Pattern pattern) throws SemanticException {
        pattern.acceptChildren(this);
    }

    public void visit(Rule rule) throws SemanticException {
        rule.acceptChildren(this);
    }

    public void visit(Specification specification) throws SemanticException {
        specification.acceptChildren(this);
    }

    public void visit(Subrule subrule) throws SemanticException {
        subrule.acceptChildren(this);
    }

    public void visit(Value value) throws SemanticException {
        value.acceptChildren(this);
    }

    public void visit(Variant variant) throws SemanticException {
        variant.acceptChildren(this);
    }
}
