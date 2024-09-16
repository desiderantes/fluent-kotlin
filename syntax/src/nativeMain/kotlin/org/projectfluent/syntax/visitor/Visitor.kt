package org.projectfluent.syntax.visitor

import org.projectfluent.syntax.ast.BaseNode
import kotlin.reflect.KClass


actual fun childrenOf(node: BaseNode) = sequence {
    when (node) {
        is BaseNode.SyntaxNode -> {
            when (node) {
                is BaseNode.SyntaxNode.Resource -> {
                    yield(Pair("body", node.body))
                }
                is BaseNode.SyntaxNode.Attribute -> {
                    yield(Pair("id", node.id))
                    yield(Pair("value", node.value))
                }
                is BaseNode.SyntaxNode.Variant -> {
                    yield(Pair("key", node.key))
                    yield(Pair("value", node.value))
                    yield(Pair("default", node.default))
                }
                is BaseNode.SyntaxNode.NamedArgument -> {
                    yield(Pair("name", node.name))
                    yield(Pair("value", node.value))
                }
                is BaseNode.SyntaxNode.Identifier -> {
                    yield(Pair("name", node.name))
                }
                is BaseNode.SyntaxNode.PatternElement -> {
                    when (node) {
                        is BaseNode.SyntaxNode.PatternElement.TextElement -> {
                            yield(Pair("value", node.value))
                        }

                        is BaseNode.SyntaxNode.PatternElement.Indent -> {
                            yield(Pair("value", node.value))
                        }
                        is BaseNode.SyntaxNode.PatternElement.Placeable -> {
                            yield(Pair("expression", node.expression))
                        }
                    }
                }

                is BaseNode.SyntaxNode.Annotation -> {
                    yield(Pair("code", node.code))
                    yield(Pair("message", node.message))
                    yield(Pair("arguments", node.arguments))
                }
                is BaseNode.SyntaxNode.CallArguments -> {
                    yield(Pair("positional", node.positional))
                    yield(Pair("named", node.named))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.FunctionReference -> {
                    yield(Pair("id", node.id))
                    yield(Pair("arguments", node.arguments))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.Literal.NumberLiteral -> {
                    yield(Pair("value", node.value))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.Literal.StringLiteral ->  {
                    yield(Pair("value", node.value))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.MessageReference -> {
                    yield(Pair("id", node.id))
                    yield(Pair("attribute", node.attribute))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.SelectExpression -> {
                    yield(Pair("selector", node.selector))
                    yield(Pair("variants", node.variants))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.TermReference -> {
                    yield(Pair("id", node.id))
                    yield(Pair("attribute", node.attribute))
                    yield(Pair("arguments", node.arguments))
                }
                is BaseNode.SyntaxNode.TopLevel.Expression.VariableReference -> {
                    yield(Pair("id", node.id))
                }
                is BaseNode.SyntaxNode.TopLevel.Pattern -> {
                    yield(Pair("elements", node.elements))
                }
                is BaseNode.SyntaxNode.TopLevel.Entry.BaseComment.Comment -> {
                    yield(Pair("content", node.content))
                }
                is BaseNode.SyntaxNode.TopLevel.Entry.BaseComment.GroupComment -> {
                    yield(Pair("content", node.content))
                }
                is BaseNode.SyntaxNode.TopLevel.Entry.BaseComment.ResourceComment -> {
                    yield(Pair("content", node.content))
                }
                is BaseNode.SyntaxNode.TopLevel.Entry.Message -> {
                    yield(Pair("id", node.id))
                    yield(Pair("value", node.value))
                    yield(Pair("attributes", node.attributes))
                    yield(Pair("comment", node.comment))
                }
                is BaseNode.SyntaxNode.TopLevel.Entry.Term -> {
                    yield(Pair("id", node.id))
                    yield(Pair("value", node.value))
                    yield(Pair("attributes", node.attributes))
                    yield(Pair("comment", node.comment))
                }
                is BaseNode.SyntaxNode.TopLevel.Junk -> {
                    yield(Pair("content", node.content))
                    yield(Pair("annotations", node.annotations))
                }
                is BaseNode.SyntaxNode.TopLevel.Whitespace -> {
                    yield(Pair("content", node.content))
                }
            }
        }

        is BaseNode.Span -> {
            yield(Pair("start", node.start))
            yield(Pair("end", node.end))
        }
    }
}

actual abstract class Visitor {

    actual fun visit(node: BaseNode) {
        if (canHandleNode(node::class)) {
            handleNode(node)
        } else {
            this.genericVisit(node)
        }
    }

    abstract fun handleNode(node: BaseNode)

    abstract fun canHandleNode(node: KClass<out BaseNode>): Boolean

    actual fun genericVisit(node: BaseNode) {
        childrenOf(node).map { (_, value) -> value }.forEach { value ->
            when (value) {
                is BaseNode -> this.visit(value)
                is Collection<*> -> value.filterIsInstance<BaseNode>().map { this.visit(it) }
            }
        }
    }
}