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

import java.io.PrintStream;
import java.util.*;

/**
 * A node of the customized tree which will be used for transformation and code
 * generation.
 * 
 * One node can have an unlimited number of children. Insertion order is
 * preserved.
 * @author Matúš Sulír
 */
public abstract class TreeNode {
    
    private TreeNode parent;
    private Set<TreeNode> childrenSet = new LinkedHashSet<TreeNode>();
    private List<TreeNode> nodesToRemove = new ArrayList<TreeNode>();
    
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
        Iterator<TreeNode> iterator = childrenSet.iterator();
        
        for (int i = 0; i < index; i++)
            iterator.next();
        
        return iterator.next();
    }
    
    /**
     * Returns all children of this node.
     * @return the iterable collection of all children
     */
    public Iterable<TreeNode> getChildren() {
        return Collections.unmodifiableSet(childrenSet);
    }
    
    /**
     * Adds a child to this node, placing it on the end.
     * @param child the child node
     */
    public void addChild(TreeNode child) {
        child.parent = this;
        childrenSet.add(child);
    }
    
    /**
     * Removes this node from the tree (including all children).
     */
    public void remove() {
        parent.childrenSet.remove(this);
        this.parent = null;
    }
    
    /**
     * Marks this node for removal.
     * 
     * <p>This is used to prevent {@link ConcurrentModificationException}.</p>
     * 
     * <p>The node will be removed immediately after parent's iteration in the
     * {@link #acceptChildren(Visitor)} method ends. If the parent node
     * currently does not perform an iteration, use the {@link #remove()}
     * method instead.</p>
     */
    public void markForRemoval() {
        parent.nodesToRemove.add(this);
    }
    
    /**
     * Calls the appropriate visitor method.
     * 
     * Subclasses should override this method in order to support the visitor
     * design pattern properly.
     * @param visitor the visitor object
     */
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Sequentially calls the {@link #accept(Visitor)} method for all children.
     * 
     * All nodes marked for removal are then removed.
     * @param visitor the visitor object
     */
    public void acceptChildren(Visitor visitor) {
        for (TreeNode child : childrenSet)
            child.accept(visitor);
        
        for (TreeNode node : nodesToRemove)
            node.remove();
    }
    
    /**
     * Prints the whole tree recursively.
     * @param outStream the stream to write to
     */
    public void dump(PrintStream outStream) {
        print(this, outStream, 0);
        outStream.println("---------------");
    }
    
    /**
     * Prints the tree node recursively.
     * @param node the node
     * @param outStream the stream to write to
     * @param indent the indentation level
     */
    private void print(TreeNode node, PrintStream outStream, int indent) {
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < indent; i++)
            output.append("  ");
        
        output.append(node);
        outStream.println(output);
        
        for (TreeNode children : node.getChildren())
            print(children, outStream, indent + 1);
    }
}
