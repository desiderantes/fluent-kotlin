package org.projectfluent.testing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.projectfluent.syntax.ast.BaseNode;
import org.projectfluent.syntax.parser.FluentParser;

import java.util.regex.Pattern;

class JavaVisitorTest {
    private final FluentParser parser = new FluentParser(false);

    @Test
    void test_wordcounter() {
        BaseNode.SyntaxNode.Resource res = parser.parse("msg = value with words");
        JavaWordCounter counter = new JavaWordCounter();
        counter.visit(res);
        assertEquals(3, counter.getWords());
    }
}
