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
package edigen.tree;

import edigen.SemanticException;
import edigen.Visitor;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A node of an abstract syntax tree (AST).
 * 
 * <p>The AST will be transformed using the visitor design pattern.</p>
 * 
 * <p>One node can have an unlimited number of children. Insertion order is
 * preserved.</p>
 * @author Matúš Sulír
 */
public abstract class TreeNode {
    
    private TreeNode parent;
    private Set<TreeNode> children = new LinkedHashSet<TreeNode>();
    
    /**
     * Returns the parent of this node.
     * 
     * @return the parent node, or null if this is the root node
     */
    public TreeNode getParent() {
        return parent;
    }
    
    /**
     * Returns the child at given index.
     * 
     * <em>Note:</em> The average time complexity of this method is O(n).
     * @param index the index, starting at 0
     * @return the child node
     */
    public TreeNode getChild(int index) {
        Iterator<TreeNode> iterator = children.iterator();
        
        for (int i = 0; i < index; i++)
            iterator.next();
        
        return iterator.next();
    }
    
    /**
     * Returns all children of this node.
     * 
     * A copy of the collection is returned to allow children removal and
     * insertion during the iteration.
     * @return the iterable collection of all children
     */
    public Iterable<TreeNode> getChildren() {
        return new ArrayList<TreeNode>(children);
    }
    
    /**
     * Returns the number of all direct children of this node.
     * @return the child count
     */
    public int childCount() {
        return children.size();
    }
    
    /**
     * Adds a child to this node, placing it on the end.
     * @param child the child node
     */
    public void addChild(TreeNode child) {
        child.parent = this;
        children.add(child);
    }
    
    /**
     * Removes this node from the tree.
     * 
     * This can be described as "tearing off" the node. The link between this
     * node and the parent one is removed bilaterally.
     */
    public void remove() {
        parent.children.remove(this);
        this.parent = null;
    }
    
    /**
     * Calls the appropriate visitor method.
     * 
     * Subclasses should override this method in order to support the visitor
     * design pattern properly.
     * @param visitor the visitor object
     */
    public void accept(Visitor visitor) throws SemanticException {
        visitor.visit(this);
    }
    
    /**
     * Sequentially calls the {@link #accept(Visitor)} method for all children.
     * @param visitor the visitor object
     */
    public void acceptChildren(Visitor visitor) throws SemanticException {
        for (TreeNode child : getChildren())
            child.accept(visitor);
    }
    
    /**
     * Prints the whole tree recursively.
     * @param outStream the stream to write to
     */
    public void dump(PrintStream outStream) {
        print(outStream, 0);
        outStream.println("---------------");
    }
    
    /**
     * Prints the tree node recursively.
     * @param outStream the stream to write to
     * @param indent the indentation level
     */
    private void print(PrintStream outStream, int indent) {
        for (int i = 0; i < indent; i++)
            outStream.print("  ");
        
        outStream.println(this);
        
        for (TreeNode child : getChildren())
            child.print(outStream, indent + 1);
    }
}
