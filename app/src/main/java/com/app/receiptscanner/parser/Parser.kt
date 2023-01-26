package com.app.receiptscanner.parser

import android.util.Log
import com.app.receiptscanner.parser.Token.Companion.TYPE_DATA
import com.app.receiptscanner.parser.Token.Companion.TYPE_FIELD
import com.app.receiptscanner.parser.TokenRelation.Companion.LINE_ABOVE
import com.app.receiptscanner.parser.TokenRelation.Companion.LINE_BELOW
import com.app.receiptscanner.parser.TokenRelation.Companion.LINE_CURRENT
import com.google.mlkit.vision.text.Text

class Parser(private val validTokens: List<TokenRelation>) {

    fun createSyntaxTree(tokens: List<Token>): Node {
        val root = Node("", null, arrayListOf(), false)
        val stack = arrayListOf<Node>()
        val lines = hashMapOf<Int, ArrayList<Token>>()
        val fields = tokens.filter { it.type == TYPE_FIELD }
        Log.e("FIELDS", fields.size.toString())
        tokens.forEach {
            if (!lines.containsKey(it.lineNumber)) lines[it.lineNumber] = arrayListOf(it)
            else lines[it.lineNumber]?.add(it)
        }
        for (field in fields) {
            Log.e("Field", field.content)
            val node = Node(field.content, root, arrayListOf(), false)
            val relation = validTokens.find { it.content.first() == field.content } ?: continue
            var count = relation.dataCount
            val lineNumbers = lines.keys.toIntArray()
            val lineNumber = when(relation.checkLines) {
                LINE_CURRENT -> field.lineNumber
                LINE_ABOVE -> {
                    if(field.lineNumber == 0) continue
                    lineNumbers[(lineNumbers.indexOf(field.lineNumber) - 1)]
                }
                LINE_BELOW -> {
                    if(field.lineNumber == lineNumbers.last()) continue
                    lineNumbers[(lineNumbers.indexOf(field.lineNumber) + 1)]
                }
                else -> field.lineNumber
            }
            if(!lines.containsKey(lineNumber)) continue
            val line = lines[lineNumber]!!
            val index = if(line.contains(field)) line.indexOf(field) + 1 else 0
            while (count > 0) {
                Log.e("LINE", line.toString())
                if(index > line.lastIndex) break
                Log.e("TR", "HERE!")
                val token = line[index]
                if(token.type == TYPE_FIELD) break
                Log.e("TR", "HERE 2!")
                node.childNodes.add(Node(token.content, node, arrayListOf(), true))
                line.remove(token)
                count -= 1
            }
            if(count != 0) continue
            root.childNodes.add(node)
        }
        printTree(root, "")
        return root
    }

    fun printTree(root: Node, indent: String = "") {
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
        val stack = arrayListOf<String>()
        val lines = text.text.split("\n").filter { it.isNotEmpty() }
        lines.forEachIndexed { lineNumber, line ->
            val textSplit = line.split(*delimiters).filter { it.isNotEmpty() }
            val textTokens = textSplit.map { content ->
                val type = when (validTokens.any { it.content.first().uppercase() == content.uppercase() }) {
                    true -> TYPE_FIELD
                    else -> TYPE_DATA
                }
                Token(type, content, lineNumber)
            }
            tokens.addAll(textTokens)
        }
        Log.e("Tokens", tokens.toString())

        return tokens
    }

    companion object {
        val delimiters = arrayOf(" ", ":", "\n")
    }
}