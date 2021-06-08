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

//    override fun equals(other: Any?) =
//        if (other is BaseNode) {
//            false
//            this.equals(other, emptySet())
//        } else {
//            false
//        }
//
//    fun equals(other: BaseNode, ignoredFields: Set<String> = setOf("span")): Boolean =
//        if (this::class == other::class) {
//            publicMemberProperties(this::class, ignoredFields).all {
//                val thisValue = it.getter.call(this)
//                val otherValue = it.getter.call(other)
//                if (thisValue is Collection<*> && otherValue is Collection<*>) {
//                    if (thisValue.size == otherValue.size) {
//                        thisValue.zip(otherValue).all { (a, b) -> scalarsEqual(a, b, ignoredFields) }
//                    } else {
//                        false
//                    }
//                } else {
//                    scalarsEqual(thisValue, otherValue, ignoredFields)
//                }
//            }
//        } else {
//            false
//        }

//    private companion object {
//        private fun publicMemberProperties(clazz: KClass<*>, ignoredFields: Set<String>) =
//            clazz.memberProperties
//                .filter { it.visibility == KVisibility.PUBLIC }
//                .filterNot { ignoredFields.contains(it.name) }
//
//        private fun scalarsEqual(left: Any?, right: Any?, ignoredFields: Set<String>) =
//            if (left is BaseNode && right is BaseNode) {
//                left.equals(right, ignoredFields)
//            } else {
//                left == right
//            }
//    }


    /**
     * Base class for AST nodes which can have Spans.
     */
    sealed class SyntaxNode : BaseNode() {
//        var span: Span? = null
//
//        fun addSpan(start: Int, end: Int) {
//            this.span = Span(start, end)
//        }

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

            data class Placeable(var expression: TopLevel.InsidePlaceable) : TopLevel.InsidePlaceable, PatternElement()
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
            val positional: MutableList<TopLevel.Expression> = mutableListOf(),
            val named: MutableList<NamedArgument> = mutableListOf()
        ) : SyntaxNode() {
            override fun equals(other: Any?): Boolean {
                return if (other != null && other is CallArguments) {
                    positional.deepEquals(other.positional) && named.deepEquals(other.named)
                } else false
            }
            override fun hashCode() = 0
        }

        data class Annotation(
            var code: String,
            var message: String,
            val arguments: MutableList<Any> = mutableListOf()
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
                    var id: Identifier,
                    var value: Pattern?,
                    var attributes: MutableList<Attribute> = mutableListOf(),
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
                    var id: Identifier,
                    var value: Pattern,
                    var attributes: MutableList<Attribute> = mutableListOf(),
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

                data class MessageReference(var id: Identifier, var attribute: Identifier? = null) : Expression()
                data class TermReference(
                    var id: Identifier,
                    var attribute: Identifier? = null,
                    var arguments: CallArguments? = null
                ) : Expression()

                data class VariableReference(var id: Identifier) : Expression()

                data class FunctionReference(var id: Identifier, var arguments: CallArguments) : Expression()

                data class SelectExpression(var selector: Expression, var variants: MutableList<Variant>) :
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
                val annotations: MutableList<Annotation> = mutableListOf()
            ) : TopLevel() {
                fun addAnnotation(annotation: Annotation) {
                    this.annotations.add(annotation)
                }

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

    data class Span(var start: Int, var end: Int) : BaseNode()
}