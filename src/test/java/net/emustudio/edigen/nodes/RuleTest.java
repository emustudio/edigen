/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.nodes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RuleTest {

    @Test
    public void testConstructorWithSingleName() {
        Rule rule = new Rule("testRule");
        
        List<String> names = rule.getNames();
        assertEquals(1, names.size());
        assertEquals("testRule", names.get(0));
    }

    @Test
    public void testConstructorWithMultipleNames() {
        List<String> nameList = Arrays.asList("rule1", "rule2", "rule3");
        Rule rule = new Rule(nameList);
        
        List<String> names = rule.getNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("rule1"));
        assertTrue(names.contains("rule2"));
        assertTrue(names.contains("rule3"));
    }

    @Test
    public void testGetNames() {
        Rule rule = new Rule(Arrays.asList("name1", "name2"));
        List<String> names = rule.getNames();
        
        assertEquals(2, names.size());
        assertEquals("name1", names.get(0));
        assertEquals("name2", names.get(1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNamesReturnsUnmodifiableList() {
        Rule rule = new Rule("test");
        rule.getNames().add("newName");
    }

    @Test
    public void testHasOnlyOneName() {
        Rule singleName = new Rule("test");
        assertTrue(singleName.hasOnlyOneName());
        
        Rule multipleNames = new Rule(Arrays.asList("test1", "test2"));
        assertFalse(multipleNames.hasOnlyOneName());
    }

    @Test
    public void testGetMethodName() {
        Rule rule = new Rule(Arrays.asList("method1", "method2"));
        assertEquals("method1", rule.getMethodName());
    }

    @Test
    public void testGetFieldNameWithParameter() {
        Rule rule = new Rule("testRule");
        String fieldName = rule.getFieldName("testRule");
        
        assertEquals("TESTRULE", fieldName);
    }

    @Test
    public void testGetFieldNameWithLowerCase() {
        Rule rule = new Rule("myrule");
        assertEquals("MYRULE", rule.getFieldName("myrule"));
    }

    @Test
    public void testGetFieldNameWithMixedCase() {
        Rule rule = new Rule("MyRule");
        assertEquals("MYRULE", rule.getFieldName("MyRule"));
    }

    @Test
    public void testGetFieldName() {
        Rule rule = new Rule(Arrays.asList("rule1", "rule2"));
        rule.setRoot(true, "rule1");
        
        assertEquals("RULE1", rule.getFieldName());
    }

    @Test
    public void testGetLabel() {
        Rule rule = new Rule("singleName");
        assertEquals("singleName", rule.getLabel());
    }

    @Test
    public void testGetLabelWithMultipleNames() {
        Rule rule = new Rule(Arrays.asList("name1", "name2", "name3"));
        String label = rule.getLabel();
        
        assertTrue(label.contains("name1"));
        assertTrue(label.contains("name2"));
        assertTrue(label.contains("name3"));
        assertTrue(label.contains(","));
    }

    @Test
    public void testIsRootDefault() {
        Rule rule = new Rule("test");
        assertFalse(rule.isRoot());
    }

    @Test
    public void testSetRoot() {
        Rule rule = new Rule(Arrays.asList("rule1", "rule2"));
        rule.setRoot(true, "rule1");
        
        assertTrue(rule.isRoot());
        assertEquals("rule1", rule.getRootRuleName());
    }

    @Test
    public void testSetRootChaining() {
        Rule rule = new Rule(Arrays.asList("rule1", "rule2"));
        Rule result = rule.setRoot(true, "rule1");
        
        assertSame(rule, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRootWithInvalidName() {
        Rule rule = new Rule(Arrays.asList("rule1", "rule2"));
        rule.setRoot(true, "invalidName");
    }

    @Test
    public void testGetRootRuleNameDefault() {
        Rule rule = new Rule("test");
        assertNull(rule.getRootRuleName());
    }

    @Test
    public void testToString() {
        Rule rule = new Rule("testRule");
        assertEquals("Rule: testRule", rule.toString());
    }

    @Test
    public void testToStringWithMultipleNames() {
        Rule rule = new Rule(Arrays.asList("rule1", "rule2"));
        String str = rule.toString();
        
        assertTrue(str.startsWith("Rule: "));
        assertTrue(str.contains("rule1"));
        assertTrue(str.contains("rule2"));
    }

    @Test
    public void testEquals() {
        Rule rule1 = new Rule(Arrays.asList("name1", "name2"));
        Rule rule2 = new Rule(Arrays.asList("name1", "name2"));
        
        assertEquals(rule1, rule2);
    }

    @Test
    public void testEqualsSameObject() {
        Rule rule = new Rule("test");
        assertEquals(rule, rule);
    }

    @Test
    public void testNotEqualsNull() {
        Rule rule = new Rule("test");
        assertNotEquals(null, rule);
    }

    @Test
    public void testNotEqualsDifferentClass() {
        Rule rule = new Rule("test");
        assertNotEquals(rule, "string");
    }

    @Test
    public void testNotEqualsDifferentNames() {
        Rule rule1 = new Rule("test1");
        Rule rule2 = new Rule("test2");
        
        assertNotEquals(rule1, rule2);
    }

    @Test
    public void testHashCode() {
        Rule rule1 = new Rule(Arrays.asList("name1", "name2"));
        Rule rule2 = new Rule(Arrays.asList("name1", "name2"));
        
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    public void testShallowCopy() {
        Rule rule = new Rule(Arrays.asList("rule1", "rule2"));
        rule.setRoot(true, "rule1");
        
        Rule copy = (Rule) rule.shallowCopy();
        
        assertNotSame(rule, copy);
        assertEquals(rule.getNames(), copy.getNames());
        assertEquals(rule.isRoot(), copy.isRoot());
        assertEquals(rule.getRootRuleName(), copy.getRootRuleName());
    }

    @Test
    public void testAccept() throws SemanticException {
        Rule rule = new Rule("test");
        final boolean[] visitedRule = {false};
        
        Visitor visitor = new Visitor() {
            @Override
            public void visit(Rule r) {
                visitedRule[0] = true;
            }
        };
        
        rule.accept(visitor);
        assertTrue(visitedRule[0]);
    }
}
