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
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;

import java.io.PrintStream;
import java.util.*;

/**
 * A node of an abstract syntax tree (AST).
 * 
 * <p>The AST will be transformed using the visitor design pattern.</p>
 * 
 * <p>One node can have an unlimited number of children. Insertion order is
 * preserved.</p>
 */
public abstract class TreeNode {
    
    private TreeNode parent;
    private final Set<TreeNode> children = new LinkedHashSet<>();
    private Integer line;
    
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
        return new ArrayList<>(children);
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
     * Adds multiple children to this node, placing them on the end.
     * @param children the child nodes
     */
    public void addChildren(TreeNode... children) {
        for (TreeNode child : children) {
            addChild(child);
        }
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
     * @throws SemanticException depends on situation
     */
    public void accept(Visitor visitor) throws SemanticException {
        visitor.visit(this);
    }
    
    /**
     * Sequentially calls the {@link #accept(Visitor)} method for all children.
     * @param visitor the visitor object
     * @throws SemanticException depends on situation
     */
    public void acceptChildren(Visitor visitor) throws SemanticException {
        for (TreeNode child : getChildren())
            child.accept(visitor);
    }

    /**
     * Returns the starting line number in the source file from which this node
     * was generated.
     * @return the line number; null if no line was associated with this node
     */
    public Integer getLine() {
        return line;
    }
    
    /**
     * Sets the starting source line number of this node.
     * 
     * It is the starting position of one of the tokens from which this tree
     * node was generated (usually the first one).
     * @param line the line number
     */
    public void setLine(Integer line) {
        this.line = line;
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
