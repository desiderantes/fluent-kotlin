package org.projectfluent.syntax.visitor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.projectfluent.syntax.ast.*
import org.projectfluent.syntax.parser.FluentParser

class TestableVisitor : Visitor() {
    var patternCount = 0
    var variantCount = 0
    var wordCount = 0
    val WORDS = Regex("\\w+")
    fun visitPattern(node: BaseNode.SyntaxNode.TopLevel.Pattern) {
        super.genericVisit(node)
        patternCount++
    }
    fun visitVariant(node: BaseNode.SyntaxNode.Variant) {
        super.genericVisit(node)
        variantCount++
    }
    fun visitTextElement(node: BaseNode.SyntaxNode.PatternElement.TextElement) {
        wordCount += WORDS.findAll(node.value).count()
    }
}

internal class VisitorTest {
    val parser = FluentParser()
    @Test
    fun test_basics() {
        val visitor = TestableVisitor()
        val res = parser.parse(
            """
            |msg = foo {${'$'}var ->
            | *[other] bar
            } baz
        """.trimMargin()
        )
        visitor.visit(res)
        assertEquals(3, visitor.wordCount)
        assertEquals(2, visitor.patternCount)
        assertEquals(1, visitor.variantCount)
    }
}

internal class ChildrenOfTest {
    @Test
    fun test_childrenOf() {
        val variant = BaseNode.SyntaxNode.Variant(
            BaseNode.SyntaxNode.Identifier("other"),
            BaseNode.SyntaxNode.TopLevel.Pattern(),
            true
        )
        val variant_props = childrenOf(variant)
        assertEquals(
            listOf("default", "key", "span", "value"),
            variant_props.map { (name, _) -> name }.sorted().toList()
        )
    }
}
