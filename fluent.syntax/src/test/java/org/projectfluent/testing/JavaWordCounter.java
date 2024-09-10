package org.projectfluent.testing;

import org.projectfluent.syntax.ast.BaseNode;
import org.projectfluent.syntax.visitor.Visitor;

import java.util.regex.Pattern;

public class JavaWordCounter extends Visitor {
    private static final java.util.regex.Pattern WORD_BOUNDARY = Pattern.compile("\\W+");

    private int words;
    public JavaWordCounter() {
        words = 0;
    }

    public int getWords() {
        return words;
    }

    public void visitResource(BaseNode.SyntaxNode.Resource node) {
        System.out.println("resource");
        this.genericVisit(node);
    }

    public void visitTextElement(BaseNode.SyntaxNode.PatternElement.TextElement node) {
        String val = node.getValue();
        words += WORD_BOUNDARY.split(val).length;
    }
}
