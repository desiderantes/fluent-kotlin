package org.projectfluent.syntax.ast

interface HasSpan {
    var span: BaseNode.Span?
}

fun HasSpan.addSpan(start: Int, end: Int) {
    this.span = BaseNode.Span(start, end)
}

/**
 * Base class for all Fluent AST nodes.
 *
 * All productions described in the ASDL subclass BaseNode, including Span and
 * Annotation.
 *
 */

private fun List<Any>.deepEquals(other: List<Any>): Boolean {
    return if (this.size == other.size) {
        this.zip(other).all { (l, r) -> l == r }
    } else false
}

sealed class BaseNode {

    /**
     * Base class for AST nodes which can have Spans.
     */
    sealed class SyntaxNode : BaseNode() {

        /**
         * A Fluent file representation
         */
        data class Resource(val body: List<TopLevel>) : SyntaxNode() {
            override fun equals(other: Any?): Boolean {
                return if (other != null && other is Resource) this.body.deepEquals(other.body)
                else false
            }

            override fun hashCode(): Int {
                return body.hashCode()
            }
        }

        data class Attribute(val id: Identifier, val value: TopLevel.Pattern) : SyntaxNode()

        data class Variant(val key: TopLevel.VariantKey, val value: TopLevel.Pattern, val default: Boolean) :
            SyntaxNode()

        data class NamedArgument(val name: Identifier, val value: TopLevel.Expression.Literal) : TopLevel.CallArgument,
            SyntaxNode()

        data class Identifier(val name: String) : TopLevel.VariantKey, SyntaxNode()

        sealed class PatternElement : SyntaxNode() {
            data class TextElement(var value: String) : PatternElement(), HasSpan {
                override var span: Span? = null
                override fun equals(other: Any?): Boolean {
                    return other != null && other is TextElement && this.value == other.value
                }

                override fun hashCode() = value.hashCode()
            }

            data class Placeable(val expression: TopLevel.InsidePlaceable) : TopLevel.InsidePlaceable, PatternElement()
            data class Indent(var value: String) : PatternElement(), HasSpan {
                override var span: Span? = null

                constructor(value: String, start: Int, end: Int) : this(value) {
                    this.addSpan(start, end)
                }

                override fun equals(other: Any?): Boolean {
                    return other != null && other is Indent && this.value == other.value
                }
                override fun hashCode() = value.hashCode()
            }
        }

        data class CallArguments(
            val positional: List<TopLevel.Expression> ,
            val named: List<NamedArgument>
        ) : SyntaxNode() {
            override fun equals(other: Any?): Boolean {
                return if (other != null && other is CallArguments) {
                    positional.deepEquals(other.positional) && named.deepEquals(other.named)
                } else false
            }
            override fun hashCode() = 0
        }

        data class Annotation(
            val code: String,
            val message: String,
            val arguments: List<Any>
        ) : SyntaxNode(), HasSpan {
            override var span: Span? = null

            override fun equals(other: Any?): Boolean {
                return if (other != null && other is Annotation) {
                    return this.code == other.code && this.message == other.message && arguments.deepEquals(other.arguments)
                }
                else false
            }
            override fun hashCode() = 0
        }

        sealed class TopLevel : SyntaxNode() {

            /**
             * An abstract base class for useful elements of Resource.body.
             */
            sealed class Entry : TopLevel() {

                data class Message(
                    val id: Identifier,
                    val value: Pattern?,
                    val attributes: List<Attribute>,
                    var comment: BaseComment.Comment? = null
                ) : Entry() {
                    override fun equals(other: Any?): Boolean {
                        return if (other != null && other is Message)
                            id == other.id && value == other.value && attributes.deepEquals(other.attributes) && comment == other.comment
                        else false
                    }
                    override fun hashCode() = 0
                }

                data class Term(
                    val id: Identifier,
                    val value: Pattern,
                    val attributes: List<Attribute>,
                    var comment: BaseComment.Comment? = null
                ) : Entry() {
                    override fun equals(other: Any?): Boolean {
                        return if (other != null && other is Term)
                            id == other.id && value == other.value && attributes.deepEquals(other.attributes) && comment == other.comment
                        else false
                    }
                    override fun hashCode() = 0
                }

                sealed class BaseComment(open val content: String) : Entry() {
                    data class Comment(override val content: String) : BaseComment(content)
                    data class GroupComment(override val content: String) : BaseComment(content)
                    data class ResourceComment(override val content: String) : BaseComment(content)
                }
            }

            data class Pattern(val elements: List<PatternElement> = listOf()) : SyntaxNode() {
                override fun equals(other: Any?): Boolean {
                    return if (other != null && other is Pattern) elements.deepEquals(other.elements) else false
                }
                override fun hashCode() = 0

                companion object {
                    operator fun invoke(vararg es: PatternElement) =
                        Pattern(es.toList()) // this can be invoked as MyClass()
                }
            }

            interface InsidePlaceable

            sealed class Expression : CallArgument, InsidePlaceable, SyntaxNode() {

                sealed class Literal(open val value: String) : Expression() {
                    data class StringLiteral(override val value: String) : Literal(value)
                    data class NumberLiteral(override val value: String) : VariantKey, Literal(value)
                }

                data class MessageReference(val id: Identifier, val attribute: Identifier? = null) : Expression()
                data class TermReference(
                    var id: Identifier,
                    var attribute: Identifier? = null,
                    var arguments: CallArguments? = null
                ) : Expression()

                data class VariableReference(val id: Identifier) : Expression()

                data class FunctionReference(val id: Identifier, val arguments: CallArguments) : Expression()

                data class SelectExpression(val selector: Expression, val variants: List<Variant>) :
                    Expression() {
                    override fun equals(other: Any?): Boolean {
                        return if (other != null && other is SelectExpression) selector == other.selector && variants.deepEquals(
                            other.variants
                        ) else false
                    }
                    override fun hashCode() = 0
                }
            }

            interface CallArgument

            interface VariantKey

            data class Junk(
                val content: String,
                val annotations: List<Annotation>
            ) : TopLevel() {

                override fun equals(other: Any?): Boolean {
                    return if (other != null && other is Junk) content == other.content && annotations.deepEquals(other.annotations) else false
                }
                override fun hashCode() = 0
            }

            /**
             * Represents top-level whitespace
             *
             * Extension of the data model in other implementations.
             */
            data class Whitespace(val content: String) : TopLevel()
        }
    }

    data class Span(val start: Int, val end: Int) : BaseNode()
}