package org.projectfluent.syntax.ast

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

/**
 * Base class for all Fluent AST nodes.
 *
 * All productions described in the ASDL subclass BaseNode, including Span and
 * Annotation.
 *
 */
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
        var span: Span? = null
        fun addSpan(start: Int, end: Int) {
            this.span = Span(start, end)
        }

        /**
         * A Fluent file representation
         */
        data class Resource(val children: List<TopLevel>) : SyntaxNode() {
            val body: MutableList<TopLevel> = mutableListOf()

            init {
                this.body += children
            }
        }

        data class Attribute(var id: Identifier, var value: TopLevel.Pattern) : SyntaxNode()

        data class Variant(var key: TopLevel.VariantKey, var value: TopLevel.Pattern, var default: Boolean) : SyntaxNode()

        data class NamedArgument(var name: Identifier, var value: TopLevel.Expression.Literal) : TopLevel.CallArgument, SyntaxNode()

        data class Identifier(var name: String) : TopLevel.VariantKey, SyntaxNode()

        sealed class PatternElement : SyntaxNode() {
            data class TextElement(var value: String) : PatternElement()
            data class Placeable(var expression: TopLevel.InsidePlaceable) : TopLevel.InsidePlaceable, PatternElement()
            data class Indent(var value: String) : PatternElement() {
                constructor(value: String, start: Int, end: Int) : this(value) {
                    this.addSpan(start, end)
                }
            }
        }

        data class CallArguments(
            val positional: MutableList<TopLevel.Expression> = mutableListOf(),
            val named: MutableList<NamedArgument> = mutableListOf()
        ) : SyntaxNode()

        data class Annotation(var code: String, var message: String) : SyntaxNode() {
            val arguments: MutableList<Any> = mutableListOf()
        }

        sealed class TopLevel : SyntaxNode() {

            /**
             * An abstract base class for useful elements of Resource.body.
             */
            sealed class Entry : TopLevel() {

                data class Message(var id: Identifier, var value: Pattern?) : Entry() {
                    var attributes: MutableList<Attribute> = mutableListOf()
                    var comment: BaseComment.Comment? = null
                }

                data class Term(var id: Identifier, var value: Pattern) : Entry() {
                    var attributes: MutableList<Attribute> = mutableListOf()
                    var comment: BaseComment.Comment? = null
                }

                sealed class BaseComment(var content: String) : Entry() {
                    data class Comment(private val myContent: String) : BaseComment(myContent)

                    data class GroupComment(private val myContent: String) : BaseComment(myContent)

                    data class ResourceComment(private val myContent: String) : BaseComment(myContent)
                }
            }

            data class Pattern(private val es: List<PatternElement>) : SyntaxNode() {
                val elements: MutableList<PatternElement> = mutableListOf()

                init {
                    this.elements += es
                }

                companion object {
                    operator fun invoke(vararg es: PatternElement) = Pattern(es.toList()) // this can be invoked as MyClass()
                }
            }

            interface InsidePlaceable

            sealed class Expression : CallArgument, InsidePlaceable, SyntaxNode() {

                sealed class Literal(val value: String) : Expression() {
                    data class StringLiteral(private val v: String) : Literal(v)
                    data class NumberLiteral(private val v: String) : VariantKey, Literal(v)
                }
                data class MessageReference(var id: Identifier, var attribute: Identifier? = null) : Expression()
                data class TermReference(
                    var id: Identifier,
                    var attribute: Identifier? = null,
                    var arguments: CallArguments? = null
                ) :
                    Expression()
                data class VariableReference(var id: Identifier) : Expression()

                data class FunctionReference(var id: Identifier, var arguments: CallArguments) : Expression()

                data class SelectExpression(var selector: Expression, var variants: MutableList<Variant>) : Expression()
            }

            interface CallArgument

            interface VariantKey

            data class Junk(val content: String) : TopLevel() {
                val annotations: MutableList<Annotation> = mutableListOf()
                fun addAnnotation(annotation: Annotation) {
                    this.annotations.add(annotation)
                }
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