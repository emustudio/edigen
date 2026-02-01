/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class ValueTest {

    private Value value;
    private Rule mockRule;

    @Before
    public void setUp() {
        value = new Value("testValue");
        mockRule = new Rule("testRule");
    }

    @Test
    public void testGetName() {
        assertEquals("testValue", value.getName());
    }

    @Test
    public void testSetAndGetRule() {
        assertNull(value.getRule());
        
        value.setRule(mockRule);
        assertEquals(mockRule, value.getRule());
    }

    @Test
    public void testGetFieldName() {
        value.setRule(mockRule);
        String fieldName = value.getFieldName();
        
        assertEquals("TESTVALUE", fieldName);
    }

    @Test
    public void testDefaultStrategies() {
        assertTrue(value.getStrategies().isEmpty());
    }

    @Test
    public void testSetAndGetStrategies() {
        value.setStrategies(Arrays.asList("big_endian", "signed"));
        
        assertEquals(2, value.getStrategies().size());
        assertTrue(value.getStrategies().contains("big_endian"));
        assertTrue(value.getStrategies().contains("signed"));
    }

    @Test
    public void testSetStrategiesWithEmptyList() {
        value.setStrategies(Collections.emptyList());
        assertTrue(value.getStrategies().isEmpty());
    }

    @Test
    public void testToString() {
        String result = value.toString();
        assertEquals("Value: testValue", result);
    }

    @Test
    public void testEquals() {
        Value value1 = new Value("test");
        Value value2 = new Value("test");
        
        assertEquals(value1, value2);
    }

    @Test
    public void testEqualsSameObject() {
        assertEquals(value, value);
    }

    @Test
    public void testNotEqualsNull() {
        assertNotEquals(null, value);
    }

    @Test
    public void testNotEqualsDifferentClass() {
        assertNotEquals(value, "string");
    }

    @Test
    public void testNotEqualsDifferentName() {
        Value value1 = new Value("test1");
        Value value2 = new Value("test2");
        
        assertNotEquals(value1, value2);
    }

    @Test
    public void testEqualsWithSameRule() {
        Value value1 = new Value("test");
        Value value2 = new Value("test");
        Rule rule = new Rule("rule");
        
        value1.setRule(rule);
        value2.setRule(rule);
        
        assertEquals(value1, value2);
    }

    @Test
    public void testNotEqualsWithDifferentRules() {
        Value value1 = new Value("test");
        Value value2 = new Value("test");
        
        value1.setRule(new Rule("rule1"));
        value2.setRule(new Rule("rule2"));
        
        assertNotEquals(value1, value2);
    }

    @Test
    public void testEqualsWithSameStrategies() {
        Value value1 = new Value("test");
        Value value2 = new Value("test");
        
        value1.setStrategies(Arrays.asList("big_endian"));
        value2.setStrategies(Arrays.asList("big_endian"));
        
        assertEquals(value1, value2);
    }

    @Test
    public void testShallowCopy() {
        value.setRule(mockRule);
        value.setStrategies(Arrays.asList("little_endian"));
        
        Value copy = (Value) value.shallowCopy();
        
        assertNotSame(value, copy);
        assertEquals(value.getName(), copy.getName());
        assertSame(value.getRule(), copy.getRule());
        assertSame(value.getStrategies(), copy.getStrategies());
    }

    @Test
    public void testAccept() throws SemanticException {
        final boolean[] visitedValue = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Value v) {
                visitedValue[0] = true;
            }
        };
        
        value.accept(visitor);
        assertTrue(visitedValue[0]);
    }
}
