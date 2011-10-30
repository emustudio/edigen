/*
 * Copyright (C) 2011 Matúš Sulír
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
package edigen.debug;

import edigen.tree.*;

/**
 *
 * @author Matúš Sulír
 */
public class PrintVisitor implements ParserVisitor {
    private int indentCount = 0;
    
    private Object print(SimpleNode node) {
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < indentCount; i++)
            output.append("  ");
        
        output.append(node);
        
        if (node.jjtGetValue() != null)
            output.append(": ").append(node.jjtGetValue());
        
        System.out.println(output);
        
        indentCount++;
        node.childrenAccept(this, null);
        indentCount--;
        
        return null;
    }
    
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    public Object visit(Start node, Object data) {
        return print(node);
    }

    public Object visit(DecoderPart node, Object data) {
        return print(node);
    }

    public Object visit(DisassemblerPart node, Object data) {
        return print(node);
    }
    
}
