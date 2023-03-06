package com.app.receiptscanner.database

import android.graphics.Rect
import com.app.receiptscanner.parser.Node
import com.app.receiptscanner.parser.Parser
import com.app.receiptscanner.parser.Token
import com.app.receiptscanner.parser.TokenField

class TestParser(private val fields: List<TokenField>) {
    fun createTestSyntaxTree(tokens: List<Token>): Node {
        val root = Node(arrayListOf(), null, arrayListOf(), false)
        var currentNode = root
        var dataCount = 0

        tokens.forEach { token ->
            if (token.type == Token.TYPE_FIELD) {
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

    fun testTokenize(text: String): List<Token> {
        val lines = text.split("\n").filterNot { it.isEmpty() }
        val tokens = arrayListOf<Token>()
        lines.forEachIndexed { lineNumber, line ->
            val elements = line.uppercase().split(*Parser.delimiters).filterNot { it.isEmpty() }
            elements.forEach { element ->
                var tokenType = Token.TYPE_DATA
                var relation: TokenField? = null
                for (field in fields) {
                    if (field.content.first().matches(element)) {
                        tokenType = Token.TYPE_FIELD
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
}