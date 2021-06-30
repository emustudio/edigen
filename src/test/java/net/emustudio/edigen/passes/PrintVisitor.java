package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.Arrays;

@SuppressWarnings("unused")
public class PrintVisitor extends Visitor {
    private int indent = 0;
    private final String title;

    public PrintVisitor() {
        this("");
    }
    public PrintVisitor(String title) {
        this.title = title;
    }

    @Override
    public void visit(Decoder decoder) throws SemanticException {
        System.out.println("\nPrintVisitor - " + title);
        decoder.acceptChildren(this);
    }

    public void visit(Mask mask) throws SemanticException {
        printAndAccept(mask);
    }

    public void visit(Pattern pattern) throws SemanticException {
        printAndAccept(pattern);
    }

    public void visit(Rule rule) throws SemanticException {
        printAndAccept(rule);

    }

    public void visit(Subrule subrule) throws SemanticException {
        printAndAccept(subrule);
    }

    public void visit(Value value) throws SemanticException {
        printAndAccept(value);
    }

    public void visit(Variant variant) throws SemanticException {
        printAndAccept(variant);
    }

    private void printAndAccept(TreeNode node) throws SemanticException {
        print(node);
        indent += 2;
        node.acceptChildren(this);
        indent -= 2;
    }

    private void print(TreeNode node) {
        char[] data = new char[indent];
        Arrays.fill(data, ' ');
        System.out.println(String.valueOf(data) + node);
    }
}
