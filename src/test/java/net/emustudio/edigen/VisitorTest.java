/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.nodes.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class VisitorTest {

    @Test
    public void testDefaultVisitTreeNode() throws SemanticException {
        final int[] visitCount = {0};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Rule rule) throws SemanticException {
                visitCount[0]++;
                super.visit(rule); // This will visit children
            }
        };
        
        Rule root = new Rule("root");
        Rule child = new Rule("child");
        root.addChild(child);
        
        visitor.visit(root);
        
        assertEquals(2, visitCount[0]); // root + child
    }

    @Test
    public void testVisitDecoder() throws SemanticException {
        final boolean[] visited = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Decoder decoder) throws SemanticException {
                visited[0] = true;
            }
        };
        
        Decoder decoder = new Decoder();
        visitor.visit(decoder);
        
        assertTrue(visited[0]);
    }

    @Test
    public void testVisitDisassembler() throws SemanticException {
        final boolean[] visited = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Disassembler disassembler) throws SemanticException {
                visited[0] = true;
            }
        };
        
        Disassembler disassembler = new Disassembler();
        visitor.visit(disassembler);
        
        assertTrue(visited[0]);
    }

    @Test
    public void testVisitRule() throws SemanticException {
        final boolean[] visited = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Rule rule) throws SemanticException {
                visited[0] = true;
            }
        };
        
        Rule rule = new Rule("test");
        visitor.visit(rule);
        
        assertTrue(visited[0]);
    }

    @Test
    public void testVisitValue() throws SemanticException {
        final boolean[] visited = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Value value) throws SemanticException {
                visited[0] = true;
            }
        };
        
        Value value = new Value("test");
        visitor.visit(value);
        
        assertTrue(visited[0]);
    }

    @Test
    public void testDefaultBehaviorAcceptsChildren() throws SemanticException {
        final int[] ruleVisits = {0};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Rule rule) throws SemanticException {
                ruleVisits[0]++;
                super.visit(rule); // Call default behavior
            }
        };
        
        Rule parent = new Rule("parent");
        Rule child1 = new Rule("child1");
        Rule child2 = new Rule("child2");
        parent.addChild(child1);
        parent.addChild(child2);
        
        visitor.visit(parent);
        
        assertEquals(3, ruleVisits[0]); // parent + 2 children
    }

    @Test
    public void testVisitorCanStopTraversal() throws SemanticException {
        final int[] visitCount = {0};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Rule rule) throws SemanticException {
                visitCount[0]++;
                // Don't call super.visit() - stops traversal
            }
        };
        
        Rule parent = new Rule("parent");
        Rule child = new Rule("child");
        parent.addChild(child);
        
        visitor.visit(parent);
        
        assertEquals(1, visitCount[0]); // Only parent, child not visited
    }

    @Test(expected = SemanticException.class)
    public void testVisitorCanThrowSemanticException() throws SemanticException {
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Rule rule) throws SemanticException {
                throw new SemanticException("Test error", rule);
            }
        };
        
        Rule rule = new Rule("test");
        visitor.visit(rule);
    }
}
