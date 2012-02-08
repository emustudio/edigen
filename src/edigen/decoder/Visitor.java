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

import edigen.decoder.tree.*;

/**
 * Generic tree node visitor.
 * 
 * The subclasses can override needed methods to implement the expected
 * behavior when visiting the particular node. Unoverriden methods will have
 * the default behavior, which is to accept all children.
 * @author Matúš Sulír
 */
public abstract class Visitor {
    public void visit(TreeNode node) {
        node.acceptChildren(this);
    }
    
    public void visit(Decoder decoder) {
        decoder.acceptChildren(this);
    }
    
    public void visit(Rule rule) {
        rule.acceptChildren(this);
    }
    
    public void visit(Variant variant) {
        variant.acceptChildren(this);
    }
    
    public void visit(Pattern pattern) {
        pattern.acceptChildren(this);
    }
    
    public void visit(Subrule subrule) {
        subrule.acceptChildren(this);
    }
}
