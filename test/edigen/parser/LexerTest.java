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
package edigen.parser;

import org.junit.Test;
import edigen.parser.gen.Token;
import edigen.parser.gen.Parser;
import java.io.StringReader;
import static org.junit.Assert.*;

/**
 * Lexical analyzer test case.
 * @author Matúš Sulír
 */
public class LexerTest {

    /**
     * Tests whether the lexer produces the given sequence of tokens from the input string.
     * 
     * EOF is appended automatically to the list of expected tokens.
     * 
     * @param input string to be tokenized
     * @param tokenKinds the sequence of token kinds expected
     */
    private void testTokens(String input, int[] tokenKinds) {
        testTokens(input, tokenKinds, new Object[0]);
    }
    
    /**
     * Tests whether the lexer produces the given sequence of tokens from the input string.
     * 
     * EOF is appended automatically to the list of expected tokens.
     * 
     * @param input string to be tokenized
     * @param tokenKinds the sequence of token kinds expected
     * @param tokenValues the sequence of token values expected
     */
    private void testTokens(String input, int[] tokenKinds, Object[] tokenValues) {
        Parser parser = new Parser(new StringReader(input));
        
        for (int i = 0; i < tokenKinds.length; i++) {
            Token token = parser.getNextToken();
            assertEquals(tokenKinds[i], token.kind);
            
            if (tokenValues.length > i)
                assertEquals(tokenValues[i], token.getValue());
        }
        
        assertEquals(parser.getNextToken().kind, Parser.EOF);
    }
    
    /**
     * Tests comments and white characters.
     */
    @Test
    public void testCommentsAndBlanks() {
        String input = "\r\n\t # comment\n#comment\r  # some comment\r\n#comment";
        testTokens(input, new int[] {});
    }
}
