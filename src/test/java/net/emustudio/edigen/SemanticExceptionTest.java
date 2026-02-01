/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.nodes.Rule;
import net.emustudio.edigen.nodes.TreeNode;
import net.emustudio.edigen.nodes.Value;
import org.junit.Test;

import static org.junit.Assert.*;

public class SemanticExceptionTest {

    @Test
    public void testExceptionWithMessage() {
        TreeNode node = new Rule("test");
        SemanticException exception = new SemanticException("Test error", node);
        
        assertEquals("Test error", exception.getMessage());
    }

    @Test
    public void testExceptionWithLineNumber() {
        TreeNode node = new Rule("test");
        node.setLine(42);
        
        SemanticException exception = new SemanticException("Test error", node);
        
        assertTrue(exception.getMessage().contains("Line 42:"));
        assertTrue(exception.getMessage().contains("Test error"));
    }

    @Test
    public void testExceptionWithoutLineNumber() {
        TreeNode node = new Value("test");
        
        SemanticException exception = new SemanticException("Test error", node);
        
        assertFalse(exception.getMessage().contains("Line"));
        assertEquals("Test error", exception.getMessage());
    }

    @Test
    public void testExceptionIsThrowable() {
        TreeNode node = new Rule("test");
        SemanticException exception = new SemanticException("Test", node);
        
        assertTrue(exception instanceof Exception);
    }

    @Test
    public void testExceptionFormattingWithLine() {
        TreeNode node = new Rule("test");
        node.setLine(10);
        
        SemanticException exception = new SemanticException("Duplicate rule name", node);
        
        assertEquals("Line 10: Duplicate rule name", exception.getMessage());
    }
}
