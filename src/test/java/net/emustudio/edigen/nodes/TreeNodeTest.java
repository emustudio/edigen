/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TreeNodeTest {

    private static class TestNode extends TreeNode {
        private final String name;

        public TestNode(String name) {
            this.name = name;
        }

        @Override
        public TreeNode shallowCopy() {
            return new TestNode(name);
        }

        @Override
        public String toString() {
            return "TestNode: " + name;
        }
    }

    private TreeNode root;
    private TreeNode child1;
    private TreeNode child2;

    @Before
    public void setUp() {
        root = new TestNode("root");
        child1 = new TestNode("child1");
        child2 = new TestNode("child2");
    }

    @Test
    public void testAddChild() {
        root.addChild(child1);
        
        assertEquals(1, root.childCount());
        assertEquals(root, child1.getParent());
    }

    @Test
    public void testAddMultipleChildren() {
        root.addChild(child1);
        root.addChild(child2);
        
        assertEquals(2, root.childCount());
        assertEquals(child1, root.getChild(0));
        assertEquals(child2, root.getChild(1));
    }

    @Test
    public void testAddChildrenVarargs() {
        root.addChildren(child1, child2);
        
        assertEquals(2, root.childCount());
        assertEquals(child1, root.getChild(0));
        assertEquals(child2, root.getChild(1));
    }

    @Test
    public void testAddChildrenList() {
        List<TreeNode> children = Arrays.asList(child1, child2);
        root.addChildren(children);
        
        assertEquals(2, root.childCount());
        assertEquals(child1, root.getChild(0));
        assertEquals(child2, root.getChild(1));
    }

    @Test
    public void testGetChildren() {
        root.addChild(child1);
        root.addChild(child2);
        
        List<TreeNode> children = root.getChildren();
        assertEquals(2, children.size());
        assertTrue(children.contains(child1));
        assertTrue(children.contains(child2));
    }

    @Test
    public void testGetParent() {
        assertNull(root.getParent());
        
        root.addChild(child1);
        assertEquals(root, child1.getParent());
    }

    @Test
    public void testRemove() {
        root.addChild(child1);
        root.addChild(child2);
        
        child1.remove();
        
        assertEquals(1, root.childCount());
        assertEquals(child2, root.getChild(0));
        assertNull(child1.getParent());
    }

    @Test
    public void testGetChild() {
        root.addChildren(child1, child2);
        
        assertEquals(child1, root.getChild(0));
        assertEquals(child2, root.getChild(1));
    }

    @Test
    public void testChildCount() {
        assertEquals(0, root.childCount());
        
        root.addChild(child1);
        assertEquals(1, root.childCount());
        
        root.addChild(child2);
        assertEquals(2, root.childCount());
    }

    @Test
    public void testLineNumber() {
        assertNull(root.getLine());
        
        root.setLine(42);
        assertEquals(Integer.valueOf(42), root.getLine());
    }

    @Test
    public void testShallowCopy() {
        root.addChild(child1);
        
        TreeNode copy = root.shallowCopy();
        assertNotSame(root, copy);
        assertEquals(0, copy.childCount());
    }

    @Test
    public void testDeepCopy() {
        root.addChild(child1);
        child1.addChild(child2);
        
        TreeNode copy = root.copy();
        
        assertNotSame(root, copy);
        assertEquals(1, copy.childCount());
        
        TreeNode copiedChild = copy.getChild(0);
        assertNotSame(child1, copiedChild);
        assertEquals(1, copiedChild.childCount());
        
        TreeNode copiedGrandchild = copiedChild.getChild(0);
        assertNotSame(child2, copiedGrandchild);
    }

    @Test
    public void testAccept() throws SemanticException {
        final boolean[] visited = {false};
        Visitor visitor = new Visitor() {
            @Override
            public void visit(TreeNode node) {
                visited[0] = true;
            }
        };
        
        root.accept(visitor);
        assertTrue(visited[0]);
    }

    @Test
    public void testAcceptChildren() throws SemanticException {
        root.addChildren(child1, child2);
        
        final int[] visitCount = {0};
        Visitor visitor = new Visitor() {
            @Override
            public void visit(TreeNode node) {
                visitCount[0]++;
            }
        };
        
        root.acceptChildren(visitor);
        assertEquals(2, visitCount[0]);
    }

    @Test
    public void testDump() {
        root.addChild(child1);
        child1.addChild(child2);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outContent);
        
        root.dump(ps);
        
        String output = outContent.toString();
        assertTrue(output.contains("TestNode: root"));
        assertTrue(output.contains("TestNode: child1"));
        assertTrue(output.contains("TestNode: child2"));
        assertTrue(output.contains("---------------"));
    }

    @Test
    public void testHashCode() {
        // Each node should have a unique hashcode based on its UUID
        assertNotEquals(root.hashCode(), child1.hashCode());
    }

    @Test
    public void testChainingAddChild() {
        TreeNode result = root.addChild(child1).addChild(child2);
        
        assertSame(root, result);
        assertEquals(2, root.childCount());
    }
}
