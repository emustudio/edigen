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
package edigen;

import edigen.decoder.tree.*;
import edigen.tree.*;
import edigen.util.BitSequence;

/**
 * The traversal which converts the parse tree to a new, customized tree.
 * 
 * <p>Rule nodes must already be added to the decoder (the root node of the tree
 * being constructed) before this pass.</p>
 * 
 * <p>The syntax tree (an output from JJTree) is not a suitable data structure
 * for direct code generation. This class traverses it with help of the visitor
 * design pattern.</p>
 * 
 * <p>The created tree will consist of nodes represented by objects in the
 * {@link edigen.decoder.tree} package. They will contain data in a form
 * appropriate for further processing (and finally, code generation) - e.g. the
 * binary masks instead of strings.</p>
 * @author Matúš Sulír
 */
public class ConvertPass implements ParserVisitor {

    private static final String NONEXISTENT_RULE = "Subrule %s refers to non-existent rule";
    
    private Decoder decoder;
    
    /**
     * Constructs the converting tree traversal.
     * @param decoder the decoder object, where the result will be stored; this
     *                decoder must already contain all rule nodes as chldren
     */
    public ConvertPass(Decoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Useless method required to support the visitor design pattern.
     * 
     * Other methods have subclasses of <code>SimpleNode</code> as the first 
     * parameter. This method is actually never called.
     * @param node something
     * @param data whatever
     * @return null
     */
    @Override
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    /**
     * Starts traversing the whole tree from the root node.
     * @param node the starting syntax tree node
     * @param data unused
     * @return the fully-populated decoder tree
     */
    @Override
    public Decoder visit(Start node, Object data) throws SemanticException {
        node.jjtGetChild(0).jjtAccept(this, data);
        
        return decoder;
    }

    /**
     * Visits the instruction decoder part of the tree.
     * @param node the decoder part node
     * @param data unused
     * @return null
     */
    @Override
    public Object visit(DecoderPart node, Object data) throws SemanticException {
        node.childrenAccept(this, data);
        
        return null;
    }
    
    /**
     * Populates rules in a RuleNameSet with variants.
     * @param node the rule node
     * @param data unused
     * @return null
     */
    @Override
    public Object visit(DecoderRule node, Object data) throws SemanticException {
        String[] ruleNames = (String[]) node.jjtGetChild(0).jjtAccept(this, data);
        int childCount = node.jjtGetNumChildren();
        
        for (String ruleName : ruleNames) {
            Rule rule = decoder.getRuleByName(ruleName);

            // all children except the first one are variants
            for (int i = 1; i < childCount; i++)
                rule.addChild((Variant) node.jjtGetChild(i).jjtAccept(this, data));
        }
        
        return null;
    }

    /**
     * Returns an array of rule names.
     * @param node the node containig a set of rule names
     * @param data unused
     * @return rule names
     */
    @Override
    public String[] visit(RuleNameSet node, Object data) throws SemanticException {
        int childCount = node.jjtGetNumChildren();
        String[] ruleNames = new String[childCount];
        
        for (int i = 0; i < childCount; i++)
            ruleNames[i] = (String) node.jjtGetChild(i).jjtAccept(this, data);
        
        return ruleNames;
    }
    
    /**
     * Returns a rule name contained in the RuleName node.
     * @param node the RuleName node
     * @param data unused
     * @return the rule name as a string
     */
    @Override
    public String visit(RuleName node, Object data) {
        return (String) node.jjtGetValue();
    }

    /**
     * Causes all children of the variant node to perform an appropriate action.
     * 
     * The children (subrules, hexadecimal and binary constants) are visited in
     * the same order as they occured in the input file. The single variant
     * object is passed as an argument to all these children and they perform
     * an appropriate action on it.
     * @param node the variant node
     * @param data unused
     * @return the populated variant object
     */
    @Override
    public Variant visit(RuleVariant node, Object data) throws SemanticException {
        Variant variant = new Variant();
        node.childrenAccept(this, variant);
        
        return variant;
    }

    /**
     * Sets the variant to return the subrule.
     * @param node the ReturnSubRule node
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(ReturnSubRule node, Object data) {
        Variant variant = (Variant) data;
        
        String name = (String) node.jjtGetValue();
        Subrule subrule = new Subrule(new Rule(name));
        variant.setReturnSubrule(subrule);
        
        return null;
    }

    /**
     * Sets the variant to return the string.
     * @param node the ReturnString node
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(ReturnString node, Object data) {
        Variant variant = (Variant) data;
        String returnString = ((String) node.jjtGetValue()).replace("\"", "");
        
        variant.setReturnString(returnString);
        
        return null;
    }

    /**
     * Adds a subrule to the variant.
     * 
     * The subrule is constructed using the name and optionally the length
     * obtained from the child / children.
     * @param node the SubRule node
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(SubRule node, Object data) throws SemanticException {
        Variant variant = (Variant) data;
        Subrule subrule;
        
        String name = (String) node.jjtGetChild(0).jjtAccept(this, data);
        Rule rule = decoder.getRuleByName(name);
        
        if (node.jjtGetNumChildren() == 1) { // without length
            if (rule == null)
                throw new SemanticException(String.format(NONEXISTENT_RULE, name));
            else
                subrule = new Subrule(rule);
        } else { // with specified length
            int length = (Integer) node.jjtGetChild(1).jjtAccept(this, data);
            
            if (rule == null) {
                if (variant.getReturnSubrule() != null && variant.getReturnSubrule().getName().equals(name))
                    subrule = new Subrule(new Rule(name), length);
                else
                    throw new SemanticException(String.format(NONEXISTENT_RULE, name));
            } else {
                subrule = new Subrule(rule, length);
            }
        }

        variant.addChild(subrule);
        
        return null;
    }
    
    /**
     * Returns the subrule name.
     * @param node the subrule node
     * @param data unused
     * @return the rule name
     */
    @Override
    public String visit(SubRuleName node, Object data) {
        return (String) node.jjtGetValue();
    }

    /**
     * Returns the subrule length.
     * @param node the subrule-length node
     * @param data unused
     * @return the length in bits
     */
    @Override
    public Integer visit(SubRuleLength node, Object data) {
        return Integer.parseInt((String) node.jjtGetValue());
    }

    /**
     * Adds a bit pattern obtained from the hexadecimal constant to the variant.
     * @param node the node containing the hexadecimal constant
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(HexConstant node, Object data) {
        Variant variant = (Variant) data;
        
        String hexString = ((String) node.jjtGetValue()).substring(2); // remove "0x"
        Pattern pattern = new Pattern(BitSequence.fromHexadecimal(hexString));
        variant.addChild(pattern);
        
        return null;
    }

    /**
     * Adds a bit pattern obtained from the binary constant to the variant.
     * @param node the node containing the binary constant
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(BinConstant node, Object data) {
        Variant variant = (Variant) data;
        
        String binaryString = (String) node.jjtGetValue();
        Pattern pattern = new Pattern(BitSequence.fromBinary(binaryString));
        variant.addChild(pattern);
        
        return null;
    }

    /**
     * The disassembler part is not yet implemented.
     * @param node the dissasembler part node
     * @param data unused
     * @return null
     */
    @Override
    public Object visit(DisassemblerPart node, Object data) {
        return null;
    }
    
    @Override
    public Object visit(DisassemblerRule node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visit(Format node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visit(Argument node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
