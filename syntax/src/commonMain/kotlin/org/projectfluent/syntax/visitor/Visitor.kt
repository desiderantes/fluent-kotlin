package org.projectfluent.syntax.visitor

import org.projectfluent.syntax.ast.BaseNode


expect fun childrenOf(node: BaseNode): Sequence<Pair<String, Any?>>

expect abstract class Visitor {
    fun visit(node: BaseNode)
    fun genericVisit(node: BaseNode)
}