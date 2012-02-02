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

import edigen.objects.BitSequence;
import edigen.objects.Decoder;
import edigen.objects.Rule;
import edigen.objects.Variant;
import edigen.tree.*;

/**
 * The AST traversal which converts the syntactic tree to a set of customized
 * objects.
 * 
 * The input AST is not a suitable data structure for direct code generation.
 * Moreover, as far as we know, it is not possible to add custom methods to
 * JJTree-generated classes (without direct modification of the generated code,
 * which is a terribly wrong idea).
 * 
 * This class traverses the AST with help of the visitor design pattern. It
 * creates a set of objects (mathematically speaking, we can call them an object
 * graph). In addition, these objects will already contain data in a form
 * appropriate for code generation - e.g. the binary masks instead of strings,
 * references to rules instead of rule names, etc. 
 * @author Matúš Sulír
 */
public class ConvertPass implements ParserVisitor {

    private Decoder decoder;
    
    /**
     * Constructs the converting tree traversal.
     * @param decoder the decoder object, where the result will be stored
     */
    public ConvertPass(Decoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Useless method required by JJTree.
     * 
     * This is required to support the visitor design pattern - other methods
     * have subclasses of <code>SimpleNode</code> as the first parameter. This
     * method is actually never called.
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
     * @param node the root node
     * @param data unused
     * @return the decoder object populated with all rules
     */
    @Override
    public Decoder visit(Start node, Object data) {
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
    public Object visit(DecoderPart node, Object data) {
        node.childrenAccept(this, data);
        
        return null;
    }
    
    /**
     * Visits the decoder rule and populates it with variants.
     * @param node the rule node
     * @param data unused
     * @return null
     */
    @Override
    public Object visit(DecoderRule node, Object data) {
        // the first child is a set of rule names
        String ruleName = (String) node.jjtGetChild(0).jjtAccept(this, data);
        
        Rule rule = decoder.getRuleByName(ruleName);
        int childCount = node.jjtGetNumChildren();
        
        // the other children are variants
        for (int i = 1; i < childCount; i++)
            rule.addVariant((Variant) node.jjtGetChild(i).jjtAccept(this, data));
        
        return null;
    }

    /**
     * Returns the first name of the given rule.
     * 
     * The first name is sufficient to identify the right rule and get a
     * reference to it.
     * @param node the node containig a set of rule names
     * @param data unused
     * @return the rule name as a string
     */
    @Override
    public String visit(RuleNameSet node, Object data) {
        return (String) node.jjtGetChild(0).jjtAccept(this, data);
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
     * The children (subrules, subrule lengths, hexadecimal and binary
     * constants) are visited in the same order as they occured in the input
     * file. The single variant object is passed as an argument to all these
     * children and they perform an appropriate action on it.
     * @param node the variant node
     * @param data unused
     * @return the populated variant object
     */
    @Override
    public Variant visit(RuleVariant node, Object data) {
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
        Rule returnRule = decoder.getRuleByName((String) node.jjtGetValue());
        
        variant.setReturnValue(returnRule);
        
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
        
        variant.setReturnValue(returnString);
        
        return null;
    }

    /**
     * Adds the subrule to the variant.
     * @param node the subrule node
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(SubRuleName node, Object data) {
        Variant variant = (Variant) data;
        Rule rule = decoder.getRuleByName((String) node.jjtGetValue());
        
        variant.getPattern().addRule(rule);
        
        return null;
    }

    /**
     * Adds the subrule length to the variant.
     * @param node the subrule-length node
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(SubRuleLength node, Object data) {
        Variant variant = (Variant) data;
        int length = Integer.parseInt((String) node.jjtGetValue());
        
        variant.getPattern().addRuleLength(length);
        
        return null;
    }

    /**
     * Adds the hexadecimal constant to the variant.
     * @param node the node containing the hexadecimal constant
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(HexConstant node, Object data) {
        Variant variant = (Variant) data;
        
        String hexString = ((String) node.jjtGetValue()).substring(2); // remove "0x"
        variant.getPattern().addConstant(BitSequence.fromHexadecimal(hexString));
        
        return null;
    }

    /**
     * Adds the binary constant to the variant.
     * @param node the node containing the binary constant
     * @param data the variant object
     * @return null
     */
    @Override
    public Object visit(BinConstant node, Object data) {
        Variant variant = (Variant) data;
        
        String binaryString = (String) node.jjtGetValue();
        variant.getPattern().addConstant(BitSequence.fromBinary(binaryString));
        
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
