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
package net.emustudio.edigen;

import net.emustudio.edigen.nodes.*;

/**
 * Generic tree node visitor.
 * <p>
 * The subclasses can override needed methods to implement the expected
 * behavior when visiting the particular node. Unoverriden methods will have
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
