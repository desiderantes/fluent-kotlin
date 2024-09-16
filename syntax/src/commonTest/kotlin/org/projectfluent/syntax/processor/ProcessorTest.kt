package org.projectfluent.syntax.processor


import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.Identifier
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.PatternElement.Placeable
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.PatternElement.TextElement
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.TopLevel.Expression.Literal.NumberLiteral
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.TopLevel.Expression.Literal.StringLiteral
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.TopLevel.Expression.SelectExpression
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.TopLevel.Pattern
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.Variant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProcessorTest {

    @Test
    fun toProcessedPattern() {
        val processor = Processor()

        var pattern = Pattern(listOf(TextElement("Hi")))
        assertEquals(pattern, processor.unescapeLiteralsToText(pattern))

        pattern = Pattern(listOf(
                Placeable(expression = StringLiteral("\\\\")),
                TextElement(" "),
                Placeable(expression = StringLiteral("""\""""))
            )
        )
        assertEquals(
            Pattern(TextElement("""\ """")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Foo "),
                Placeable(expression = StringLiteral("Bar"))
            )
        )
        assertEquals(
            Pattern(TextElement("Foo Bar")),
            processor.unescapeLiteralsToText(pattern)
        )
        // The original Pattern isn't modified.
        assertEquals(
            Pattern(
                TextElement("Foo "),
                Placeable(expression = StringLiteral("Bar"))
            ),
            pattern
        )

        pattern = Pattern(listOf(
                TextElement("Hi,"),
                Placeable(expression = StringLiteral("""\u0020""")),
                TextElement("there")
            )
        )
        assertEquals(
            Pattern(TextElement("Hi, there")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Emoji: "),
                Placeable(expression = StringLiteral("""\U01f602"""))
            )
        )
        assertEquals(
            Pattern(TextElement("Emoji: \uD83D\uDE02")),
            processor.unescapeLiteralsToText(pattern)
        )
        assertEquals(
            Pattern(TextElement("Emoji: 😂")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Illegal escape sequence: "),
                Placeable(expression = StringLiteral("""\ud800"""))
            )
        )
        assertEquals(
            Pattern(TextElement("Illegal escape sequence: �")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Illegal escape sequence: "),
                Placeable(expression = StringLiteral("""\U00d800"""))
            )
        )
        assertEquals(
            Pattern(TextElement("Illegal escape sequence: �")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Hi, "),
                Placeable(expression = StringLiteral("""{""")),
                TextElement(" there")
            )
        )
        assertEquals(
            Pattern(TextElement("Hi, { there")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Foo\n"),
                Placeable(expression = StringLiteral(""".""")),
                TextElement("bar")
            )
        )
        assertEquals(
            Pattern(TextElement("Foo\n.bar")),
            processor.unescapeLiteralsToText(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Foo "),
                Placeable(
                    expression = SelectExpression(
                        NumberLiteral("1"),
                        mutableListOf(
                            Variant(
                                Identifier("other"),
                                Pattern(
                                    TextElement("bar "),
                                    Placeable(
                                        StringLiteral("{-_-}")
                                    )
                                ),
                                true
                            )
                        )
                    )
                ),
                TextElement(" baz")
            )
        )
        assertEquals(
            Pattern(
                TextElement("Foo "),
                Placeable(
                    expression = SelectExpression(
                        NumberLiteral("1"),
                        mutableListOf(
                            Variant(
                                Identifier("other"),
                                Pattern(TextElement("bar {-_-}")),
                                true
                            )
                        )
                    )
                ),
                TextElement(" baz")

            ),
            processor.unescapeLiteralsToText(pattern)
        )
    }

    @Test
    fun toRawPattern() {
        val processor = Processor()

        var pattern = Pattern(listOf(
                TextElement("Hi")
            )
        )
        assertEquals(pattern, processor.escapeTextToLiterals(pattern))

        pattern = Pattern(listOf(
                TextElement("""\ """")
            )
        )
        assertEquals(pattern, processor.escapeTextToLiterals(pattern))

        pattern = Pattern(listOf(
                TextElement("Hi, {-_-}")
            )
        )
        assertEquals(
            Pattern(
                TextElement("Hi, "),
                Placeable(expression = StringLiteral("{")),
                TextElement("-_-"),
                Placeable(expression = StringLiteral("}"))
            ),
            processor.escapeTextToLiterals(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("Foo\nbar")
            )
        )
        assertEquals(pattern, processor.escapeTextToLiterals(pattern))

        pattern = Pattern(listOf(
                TextElement("Foo\n*bar")
            )
        )
        assertEquals(
            Pattern(
                TextElement("Foo\n"),
                Placeable(expression = StringLiteral("*")),
                TextElement("bar")
            ),
            processor.escapeTextToLiterals(pattern)
        )

        pattern = Pattern(listOf(
                TextElement("\nFoo\nbar    ")
            )
        )
        assertEquals(
            Pattern(
                Placeable(expression = StringLiteral("")),
                TextElement("\nFoo\nbar    "),
                Placeable(expression = StringLiteral(""))
            ),
            processor.escapeTextToLiterals(pattern)
        )
    }
}
