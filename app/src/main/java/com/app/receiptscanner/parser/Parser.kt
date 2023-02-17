package com.app.receiptscanner.parser

import android.graphics.Rect
import android.util.Log
import com.app.receiptscanner.parser.Token.Companion.TYPE_DATA
import com.app.receiptscanner.parser.Token.Companion.TYPE_FIELD
import com.app.receiptscanner.parser.TokenField.Companion.CHECK_ABOVE
import com.app.receiptscanner.parser.TokenField.Companion.CHECK_AFTER
import com.app.receiptscanner.parser.TokenField.Companion.CHECK_BEFORE
import com.app.receiptscanner.parser.TokenField.Companion.CHECK_BELOW
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Element
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class Parser(private val fields: List<TokenField>) {
    fun createSyntaxTree(relations: List<TokenRelationship>): Node {
        val fields = relations.filter { it.token.type == TYPE_FIELD }
        val root = Node(arrayListOf(), null, arrayListOf(), false)
        fields.forEach { field ->
            val relationship = field.token.relation ?: return@forEach
            var dataCount = relationship.dataCount
            var currentRelation: TokenRelationship? = field
            val decrementCount = relationship.dataCount != -1
            if (!decrementCount) dataCount = 1
            val node = Node(field.token.content, root, arrayListOf(), false)
            while (currentRelation != null) {
                when (relationship.flag) {
                    CHECK_ABOVE -> currentRelation = currentRelation.above
                    CHECK_BELOW -> currentRelation = currentRelation.below
                    CHECK_AFTER -> currentRelation = currentRelation.right
                    CHECK_BEFORE -> currentRelation = currentRelation.left
                }
                if (currentRelation == null || currentRelation.token.type == TYPE_FIELD || dataCount <= 0) break
                val currentNode =
                    Node(currentRelation.token.content, node, arrayListOf(), true)
                node.childNodes.add(currentNode)
                if (decrementCount) dataCount -= 1
            }
            root.childNodes.add(node)
        }
        return root
    }

    fun createTestSyntaxTree(tokens: List<Token>): Node {
        val root = Node(arrayListOf(), null, arrayListOf(), false)
        var currentNode = root
        var dataCount = 0

        tokens.forEach { token ->
            if (token.type == TYPE_FIELD) {
                currentNode = Node(token.content, root, arrayListOf(), false)
                root.childNodes.add(currentNode)
                dataCount = token.relation?.dataCount ?: -1
            } else {
                if (dataCount > 0) {
                    dataCount -= 1
                    val node = Node(token.content, currentNode, arrayListOf(), true)
                    currentNode.childNodes.add(node)
                }
            }
        }
        return root
    }

    private fun printTree(root: Node, indent: String = "") {
        Log.e("TR", "$indent${root.content}")
        if (root.childNodes.isEmpty()) {
            Log.e("TR", "")
            return
        }
        Log.e("TR", " {")
        for (node in root.childNodes) {
            printTree(node, "$indent   ")
        }
        Log.e("TR", "$indent}")
    }

    fun tokenize(text: Text): List<Token> {
        val tokens = arrayListOf<Token>()
        val elements = arrayListOf<Element>()
        text.textBlocks.forEach {
            it.lines.forEach { line ->
                elements.addAll(line.elements)
            }
        }
        elements.sortBy { it.boundingBox?.centerY() }
        var pivot: Element = elements[0]
        val elementArray = arrayListOf<Pair<Element, Int>>()
        var currentLine = 0
        elementArray.add(Pair(elements[0], 0))
        val lower = cos((PI / 180.0f) * MAX_ANGLE)
        for (i in 1 until elements.size) {
            val dx = (elements[i].boundingBox!!.centerX() - pivot.boundingBox!!.centerX()).toFloat()
            val dy = (elements[i].boundingBox!!.centerY() - pivot.boundingBox!!.centerY()).toFloat()
            val unitX = abs(dx / sqrt(dx * dx + dy * dy))
            if (unitX < lower || unitX > 1.0f) {
                pivot = elements[i]
                currentLine++
            }
            elementArray.add(Pair(elements[i], currentLine))
        }
        val comparator =
            compareBy<Pair<Element, Int>> { it.second }.thenBy { it.first.boundingBox?.centerX() }
        elementArray.sortWith(comparator)
        elementArray.forEach { pair ->
            val element = pair.first
            val number = pair.second
            val elementSplit =
                element.text.uppercase().split(*delimiters).filter { it.isNotEmpty() }
            var index = 0
            while (index < elementSplit.size) {
                val matches =
                    fields.filter { it.content.first().matches(elementSplit[index]) }
                if (matches.isEmpty()) {
                    val token = Token(
                        TYPE_DATA,
                        arrayListOf(elementSplit[index]),
                        number,
                        element.boundingBox!!
                    )
                    tokens.add(token)
                } else {
                    val initialIndex = index
                    var found = false
                    val contentArray = arrayListOf<String>()
                    var matchIndex = 0
                    while (matchIndex < matches.size) {
                        if (initialIndex + matches[matchIndex].content.size > elementSplit.size) {
                            matchIndex++
                            continue
                        }
                        found = true
                        contentArray.clear()
                        for (i in matches[matchIndex].content.indices) {
                            if (matches[matchIndex].content[i].matches(elementSplit[initialIndex + i])) {
                                contentArray.add(elementSplit[initialIndex + i])
                            } else {
                                found = false
                            }
                        }
                        if (found) break
                        matchIndex++
                    }
                    if (found) {
                        val token = Token(
                            TYPE_FIELD,
                            contentArray,
                            number,
                            element.boundingBox!!,
                            matches[matchIndex]
                        )
                        tokens.add(token)
                        index = initialIndex + matches[matchIndex].content.size
                    }
                }
                index++
            }
        }

        return tokens
    }

    fun testTokenize(text: String): List<Token> {
        val lines = text.split("\n").filterNot { it.isEmpty() }
        val tokens = arrayListOf<Token>()
        lines.forEachIndexed { lineNumber, line ->
            val elements = line.uppercase().split(*delimiters).filterNot { it.isEmpty() }
            elements.forEach { element ->
                var tokenType = TYPE_DATA
                var relation: TokenField? = null
                for (field in fields) {
                    if (field.content.first().matches(element)) {
                        tokenType = TYPE_FIELD
                        relation = field
                        break
                    }
                }
                val token = Token(tokenType, arrayListOf(element), lineNumber, Rect(), relation)
                tokens.add(token)
            }
        }
        return tokens
    }

    fun generateRelations(tokens: List<Token>): List<TokenRelationship> {
        val relationships = tokens.map { TokenRelationship(it) }
        val lines = hashMapOf<Int, ArrayList<TokenRelationship>>()
        relationships.forEach { relation ->
            val lineNumber = relation.token.lineNumber
            if (lines.containsKey(lineNumber)) {
                lines[lineNumber]?.add(relation)
            } else {
                lines[lineNumber] = arrayListOf(relation)
            }
        }
        lines.keys.forEach { lineNumber ->
            lines[lineNumber]?.sortBy { relation -> relation.token.boundingBox.centerX() }
        }
        relationships.forEach { relation ->
            val line = lines[relation.token.lineNumber] ?: return@forEach
            val lineAbove = lines[relation.token.lineNumber - 1]
            val lineBelow = lines[relation.token.lineNumber + 1]
            val index = line.indexOf(relation)

            lineAbove?.let {
                relation.above = it.minByOrNull { relation2 ->
                    abs(relation.token.boundingBox.centerX() - relation2.token.boundingBox.centerX())
                }
            }
            lineBelow?.let {
                relation.below = it.minByOrNull { relation2 ->
                    abs(relation.token.boundingBox.centerX() - relation2.token.boundingBox.centerX())
                }
            }
            if (index != 0) {
                relation.left = line[index - 1]
            }
            if (index != line.lastIndex) {
                relation.right = line[index + 1]
            }
        }
        return relationships
    }

    companion object {
        const val MAX_ANGLE = 2
        val delimiters = arrayOf(" ", ",", ":", "\n", "\t", "-")
    }
}