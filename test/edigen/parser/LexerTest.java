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
import static edigen.parser.gen.Parser.*;

/**
 * The lexical analyzer test case.
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
     * @param tokenImages the sequence of token images (string representations) expected
     */
    private void testTokens(String input, int[] tokenKinds, Object[] tokenImages) {
        Parser parser = new Parser(new StringReader(input));
        
        for (int i = 0; i < tokenKinds.length; i++) {
            Token token = parser.getNextToken();
            assertEquals(tokenKinds[i], token.kind);
            
            if (tokenImages.length > i)
                assertEquals(tokenImages[i], token.image);
        }
        
        assertEquals(parser.getNextToken().kind, EOF);
    }
    
    /**
     * Tests comments and white characters.
     */
    @Test
    public void testCommentsAndBlanks() {
        String input = "\r\n\t # comment\n#comment\r  # some comment\r\n#comment";
        testTokens(input, new int[] {});
    }
    
    /**
     * Tests operator lexical analysis.
     */
    @Test
    public void testOperators() {
        String input = "= ;:| %%";
        int[] expected = {EQUALS, SEMICOLON, COLON, OR, PART_SEPARATOR};
        
        testTokens(input, expected);
    }
    
    /**
     * Tests identifiers and string literals.
     */
    @Test
    public void testIDsAndStrings() {
        String input = "ident \"string\" _Id3ent_\"some string\t2\"";
        int[] kinds = {ID, STRING, ID, STRING};
        String[] images = {"ident", "\"string\"", "_Id3ent_", "\"some string\t2\""};
        
        testTokens(input, kinds, images);
    }
    
    /**
     * Tests numeric literals.
     */
    @Test
    public void testNumbers() {
        String input = "0110 0xFF 0x1A2 (123) ( 10 )";
        int[] kinds = {BIN_NUMBER, HEX_NUMBER, HEX_NUMBER,
            LEFT_PAR, DEC_NUMBER, RIGHT_PAR, LEFT_PAR, DEC_NUMBER, RIGHT_PAR};
        String[] images = {"0110", "0xFF", "0x1A2", "(", "123", ")", "(", "10", ")"};
        
        testTokens(input, kinds, images);
    }
}
