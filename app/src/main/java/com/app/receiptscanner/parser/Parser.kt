package com.app.receiptscanner.parser

import android.util.Log
import com.app.receiptscanner.parser.Token.Companion.TYPE_DATA
import com.app.receiptscanner.parser.Token.Companion.TYPE_FIELD
import com.app.receiptscanner.parser.TokenRelation.Companion.CHECK_ABOVE
import com.app.receiptscanner.parser.TokenRelation.Companion.CHECK_AFTER
import com.app.receiptscanner.parser.TokenRelation.Companion.CHECK_BEFORE
import com.app.receiptscanner.parser.TokenRelation.Companion.CHECK_BELOW
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Element
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class Parser(private val fields: List<TokenRelation>) {
    fun createSyntaxTree(tokens: List<Token>): Node {
        val root = Node(arrayListOf(), null, arrayListOf(), false)
        val lines = hashMapOf<Int, ArrayList<Token>>()
        val fields = tokens.filter { it.type == TYPE_FIELD }
        tokens.forEach {
            if (!lines.containsKey(it.lineNumber)) lines[it.lineNumber] = arrayListOf(it)
            else lines[it.lineNumber]?.add(it)
        }
        fields.forEach {
            val node = Node(it.content, root, arrayListOf(), false)
            val relation = it.relation ?: return@forEach
            var count = relation.dataCount
            val lineNumber = when (relation.flag) {
                CHECK_AFTER, CHECK_BEFORE -> it.lineNumber
                CHECK_ABOVE -> {
                    if (it.lineNumber == 0) return@forEach
                    else it.lineNumber - 1
                }
                CHECK_BELOW -> {
                    if (it.lineNumber == lines.keys.last()) return@forEach
                    else it.lineNumber + 1
                }
                else -> it.lineNumber
            }
            val line = lines[lineNumber] ?: return@forEach
            val index = if (line.contains(it)) line.indexOf(it) else 0
            if (count < 0) {
                val start = when (relation.flag) {
                    CHECK_AFTER -> index + 1
                    CHECK_BEFORE, CHECK_ABOVE, CHECK_BELOW -> 0
                    else -> 0
                }
                val end = when (relation.flag) {
                    CHECK_AFTER, CHECK_ABOVE, CHECK_BELOW -> line.size
                    CHECK_BEFORE -> index
                    else -> line.size
                }
                val nodes = line.subList(start, end).toList().mapNotNull { token ->
                    if (token.type == TYPE_FIELD) null
                    else Node(token.content, node, arrayListOf(), true)
                }
                node.childNodes.addAll(nodes)
                val removalTokens = (start until end).map { i -> line[i] }
                line.removeAll(removalTokens.toSet())
                root.childNodes.add(node)
            } else {
                val removalTokens = arrayListOf<Token>()
                for (i in index + 1 until index + 1 + count) {
                    if (i > line.lastIndex) break
                    val token = line[index]
                    if (token.type == TYPE_FIELD) break
                    node.childNodes.add(Node(token.content, node, arrayListOf(), true))
                    removalTokens.add(token)
                    count -= 1
                }
                if (count != 0) return@forEach
                line.removeAll(removalTokens.toSet())
                root.childNodes.add(node)
            }
        }
        printTree(root, "")
        return root
    }

    fun createTestSyntaxTree(tokens: List<Token>): Node {
        val root = Node(arrayListOf(), null, arrayListOf(), false)
        var currentNode = root
        var dataCount = 0

        tokens.forEach { token ->
            if(token.type == TYPE_FIELD) {
                currentNode = Node(token.content, root, arrayListOf(), false)
                root.childNodes.add(currentNode)
                dataCount = token.relation?.dataCount ?: -1
            } else {
                if(dataCount > 0) {
                    dataCount -= 1
                    val node = Node(token.content, currentNode, arrayListOf(), true)
                    currentNode.childNodes.add(node)
                }
            }
        }
        return root
    }

//    fun createSyntaxTree(tokens: List<Token>): Node {
//        val root = Node("", null, arrayListOf(), false)
//        val stack = arrayListOf<Node>()
//        val lines = hashMapOf<Int, ArrayList<Token>>()
//        val fields = tokens.filter { it.type == TYPE_FIELD }
//        Log.e("FIELDS", fields.size.toString())
//        tokens.forEach {
//            if (!lines.containsKey(it.lineNumber)) lines[it.lineNumber] = arrayListOf(it)
//            else lines[it.lineNumber]?.add(it)
//        }
//        for (field in fields) {
//            Log.e("Field", field.content)
//            val node = Node(field.content, root, arrayListOf(), false)
//            val relation = validTokens.find { it.content.first() == field.content } ?: continue
//            var count = relation.dataCount
//            val lineNumbers = lines.keys.toIntArray()
//            val lineNumber = when (relation.flag) {
//                CHECK_AFTER -> field.lineNumber
//                CHECK_ABOVE -> {
//                    if (field.lineNumber == 0) continue
//                    lineNumbers[(lineNumbers.indexOf(field.lineNumber) - 1)]
//                }
//                CHECK_BELOW -> {
//                    if (field.lineNumber == lineNumbers.last()) continue
//                    lineNumbers[(lineNumbers.indexOf(field.lineNumber) + 1)]
//                }
//                else -> field.lineNumber
//            }
//            if (!lines.containsKey(lineNumber)) continue
//            val line = lines[lineNumber]!!
//            val index = if (line.contains(field)) line.indexOf(field) + 1 else 0
//            while (count > 0) {
//                Log.e("LINE", line.toString())
//                if (index > line.lastIndex) break
//                Log.e("TR", "HERE!")
//                val token = line[index]
//                if (token.type == TYPE_FIELD) break
//                Log.e("TR", "HERE 2!")
//                node.childNodes.add(Node(token.content, node, arrayListOf(), true))
//                line.remove(token)
//                count -= 1
//            }
//            if (count != 0) continue
//            root.childNodes.add(node)
//        }
//        printTree(root, "")
//        return root
//    }


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
        val lower = cos((PI / 180.0f) * 5)
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
                    val token = Token(TYPE_DATA, arrayListOf(elementSplit[index]), number)
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
                        val token = Token(TYPE_FIELD, contentArray, number, matches[matchIndex])
                        tokens.add(token)
                        index = initialIndex + matches[matchIndex].content.size
                    }
                }
                index++
            }
        }
        var index = 0
        while (index < elementArray.size) {
            val element = elementArray[index].first
            val lineNumber = elementArray[index].second
            val sanitized = element.text.uppercase().filter { !delimiters.contains(it.toString()) }
            val initialIndex = index
            val matches = fields.filter { it.content.first().matches(sanitized) }
            if (matches.isEmpty()) {
                val token = Token(TYPE_DATA, arrayListOf(sanitized), lineNumber)
                tokens.add(token)
            } else {
                val fieldEntries = arrayListOf<String>()
                var matchIndex = 0
                var found = false
                while (matchIndex < matches.size) {
                    val lastIndex = initialIndex + matches[matchIndex].content.size
                    val endLine = elementArray[lastIndex.coerceAtMost(elementArray.lastIndex)]
                        .second != lineNumber
                    if (lastIndex > elementArray.size || endLine) {
                        matchIndex++
                        continue
                    }
                    found = true
                    fieldEntries.clear()
                    for (i in matches[matchIndex].content.indices) {
                        val entry = elementArray[initialIndex + i].first.text
                        if (matches[matchIndex].content[i].matches(entry)) {
                            fieldEntries.add(entry)
                        } else {
                            found = false
                        }
                    }
                    if (found) break
                    matchIndex++
                }
                if (found) {
                    val token = Token(TYPE_FIELD, fieldEntries, lineNumber, matches[matchIndex])
                    tokens.add(token)
                    index = initialIndex + matches[matchIndex].content.size
                }
            }
            index++
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
                var relation: TokenRelation? = null
                for(field in fields) {
                    if(field.content.first().matches(element)) {
                        tokenType = TYPE_FIELD
                        relation = field
                        break
                    }
                }
                val token = Token(tokenType, arrayListOf(element), lineNumber, relation)
                tokens.add(token)
            }
        }
        return tokens
    }
    companion object {
        val delimiters = arrayOf(" ", ",", ":", "\n", "\t", "-")
    }
}