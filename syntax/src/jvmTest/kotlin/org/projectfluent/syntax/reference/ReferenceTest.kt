package org.projectfluent.syntax.reference

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.projectfluent.syntax.ast.BaseNode
import org.projectfluent.syntax.ast.BaseNode.SyntaxNode.TopLevel.Whitespace
import org.projectfluent.syntax.parser.FluentParser
import java.io.File
import java.nio.file.Paths
import org.projectfluent.syntax.visitor.childrenOf

open class ReferenceTest {
    open val fixture = "reference_fixtures"
    open val DISABLED = arrayOf(
        "leading_dots.ftl",
        ""
    )
    @TestFactory
    open fun references(): Iterable<DynamicTest> {
        val referenceTests: MutableList<DynamicTest> = mutableListOf()
        val referencedir = Paths.get("src", "test", "resources", fixture)
        for (entry in referencedir.toFile().walk()) {
            if (entry.extension == "ftl" && ! DISABLED.contains(entry.name)) {
                val reftest = DynamicTest.dynamicTest(entry.name) {
                    this.compareReference(entry)
                }
                referenceTests.add(reftest)
            }
        }
        return referenceTests
    }

    fun compareReference(ftlFile: File) {
        val jsonFile = File(ftlFile.path.replace(".ftl", ".json"))
        val refContent = ftlFile.readText()
        val parser = FluentParser()
        val resource = parser.parse(refContent)
        val ref = Parser.default().parse(jsonFile.path) as JsonObject
        assertAstEquals(ref, resource)
    }

    private fun assertAstEquals(jsonObject: JsonObject, node: BaseNode, stack: MutableList<String> = mutableListOf()) {
        val jType = jsonObject.remove("type")
        assertEquals(jType, node::class.simpleName, stack.joinToString(">"))
        for ((key, value) in childrenOf(node)) {
            if (key == "span") {
                value?.let {
                    val expected = jsonObject.obj(key)
                    expected?.let {
                        assertAstEquals(expected, value as BaseNode, stack)
                    }
                }
                return
            }
            stack.add(key)
            val jVal = jsonObject[key]
            if (jVal == null) {
                assertNull(value, "${stack.joinToString(">")} is expected to be null")
            }
            when (value) {
                null -> assertNull(jVal, "${stack.joinToString(">") }} is expected to be not null")
                is BaseNode -> assertAstEquals(jVal as JsonObject, value, stack)
                is Collection<*> -> {
                    assertTrue(jVal is JsonArray<*>, "${stack.joinToString(">")} is not a list")
                    value.filterNot { it is Whitespace }.zip(jVal as JsonArray<*>).forEachIndexed { index, pair ->
                        val (childNode, childJson) = pair
                        stack.add(index.toString())
                        when (childNode) {
                            is BaseNode -> {
                                assertTrue(
                                    childJson is JsonObject,
                                    "${stack.joinToString(">")} is not expected to be a node"
                                )
                                assertAstEquals(childJson as JsonObject, childNode, stack)
                            }
                            // Compare Fluent values as strings
                            else -> assertEquals(childJson.toString(), "$childNode")
                        }
                        stack.removeAt(stack.lastIndex)
                    }
                }
                else -> assertEquals(jVal, value, "${stack.joinToString(">")} differs")
            }
            stack.removeAt(stack.lastIndex)
        }
    }
}

class StructureTest : ReferenceTest() {
    override val fixture = "structure_fixtures"
    override val DISABLED = arrayOf(
        ""
    )
}
