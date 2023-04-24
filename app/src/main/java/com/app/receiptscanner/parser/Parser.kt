package com.app.receiptscanner.parser

import com.app.receiptscanner.parser.FieldTemplate.CHECK_ABOVE
import com.app.receiptscanner.parser.FieldTemplate.CHECK_AFTER
import com.app.receiptscanner.parser.FieldTemplate.CHECK_BEFORE
import com.app.receiptscanner.parser.FieldTemplate.CHECK_BELOW
import com.app.receiptscanner.parser.Token.Companion.TYPE_DATA
import com.app.receiptscanner.parser.Token.Companion.TYPE_FIELD
import com.app.receiptscanner.storage.NormalizedReceipt
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Element
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class Parser(private val fieldMap: FieldMap) {

    /**
     * Creates an Abstract Syntax Tree of depth 2 from the tokenRelationships generated from the generateRelations
     * The child nodes of the root are the fields for the receipt, whilst the childnodes of a field is the data
     * associated with said field
     *
     * @param relations a list of TokenRelationships from the generateRelations method
     * @return the root Node of the syntax tree.
     **/
    fun createSyntaxTree(relations: List<TokenRelationship>): Node {
        val fieldTokens = relations.filter { it.token.type == TYPE_FIELD }
        val root = Node(arrayListOf(), null, arrayListOf(), false)

        // For each field the tokens above are stored as child nodes until it either
        // has no tokens in the specified direction, runs into another field or all
        // the data necessary for the field has been acquired
        fieldTokens.forEach { token ->
            //stops evaluating this field if there is no associated TokenField
            val field = token.token.field ?: return@forEach

            var dataCount = field.dataCount
            var currentRelation: TokenRelationship? = token
            val decrementCount = field.dataCount != -1
            if (!decrementCount) dataCount = 1
            val node = Node(token.token.content, root, arrayListOf(), false, token.token.regex)
            while (currentRelation != null) {
                when (field.direction) {
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

    /**
     * Splits the result of MLKit OCR into Tokens for further processing. elements are first split
     * into lines based on their angle from a given pivot element, and then ordered by x position,
     * before finally being filtered of illegal characters and assigned a token.
     * If the element matches one of the valid fields, it is assigned a type of TYPE_FIELD and
     * has the field stored with it.
     * Otherwise it has a type of TYPE_DATA
     *
     * @param text the Text Object produced via MLKit OCR
     * @return a list of tokens ordered first by line, then by x position
     * @see Text
     */
    fun tokenize(text: Text): List<Token> {
        val tokens = arrayListOf<Token>()
        val elements = arrayListOf<Element>()
        text.textBlocks.forEach {
            it.lines.forEach { line ->
                elements.addAll(line.elements)
            }
        }
        //Exits early if the OCR found no text
        if (elements.isEmpty()) return tokens
        elements.sortBy { it.boundingBox?.centerY() }
        var pivot: Element = elements[0]
        val elementArray = arrayListOf<Pair<Element, Int>>()
        var currentLine = 0
        elementArray.add(Pair(elements[0], 0))
        val lower = cos((PI / 180.0f) * MAX_ANGLE)

        // - Line construction section -
        //  Iterates through the elements and compares the angle between the pivot element and the
        // current element with the maximum angle allowed for a line. If the angle is lower than
        // the maximum error the element is added to the current line, otherwise it becomes the
        // pivot and starts a new line
        for (i in 1 until elements.size) {
            val dx = (elements[i].boundingBox!!.centerX() - pivot.boundingBox!!.centerX()).toFloat()
            val dy = (elements[i].boundingBox!!.centerY() - pivot.boundingBox!!.centerY()).toFloat()

            // Calculates the cosine of the angle between the line connecting
            // current element and the pivot, and the x axis
            val unitX = abs(dx / sqrt(dx * dx + dy * dy))
            if (unitX < lower) {
                pivot = elements[i]
                currentLine++
            }
            elementArray.add(Pair(elements[i], currentLine))
        }
        // Sorts the elements by their x position, so that they can be evaluated in reading order
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
                var regex = Regex(elementSplit[index])
                val matches =
                    fieldMap.filter { it.key.first().matches(regex) }.toList()
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
                        if (initialIndex + matches[matchIndex].first.size > elementSplit.size) {
                            matchIndex++
                            continue
                        }
                        found = true
                        contentArray.clear()
                        for (i in matches[matchIndex].first.indices) {
                            regex = Regex(elementSplit[initialIndex + i])
                            if (matches[matchIndex].first[i].matches(regex)) {
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
                            matches[matchIndex].second,
                            matches[matchIndex].first
                        )
                        tokens.add(token)
                        index = initialIndex + matches[matchIndex].first.size
                    }
                }
                index++
            }
        }

        return tokens
    }

    /**
     * Generates a set of relations which store references to the tokens surrounding each token.
     * A relation is null if there is no token adjacent to it in the specified direction
     *
     * @param tokens a list of tokens produced through the tokenize method
     * @return a list of TokenRelationships which store references to the surrounding tokens
     **/
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

    fun createReceipt(root: Node, type: Int): NormalizedReceipt? {
        val nodeFields = root.childNodes
        if (nodeFields.isEmpty()) return null

        val template = FieldTemplate.getFieldsById(type)
        nodeFields.forEach {
            val data = arrayListOf<String>()
            it.childNodes.forEach { node ->
                data.addAll(node.content)
            }
            it.regex?.let { regex -> template.setField(regex, data) }
        }
        val calendar = Calendar.getInstance()
        val currentDate = calendar.timeInMillis
        return NormalizedReceipt(-1, "", currentDate, "", type, template)
    }

    companion object {
        const val MAX_ANGLE = 2.0f
        val delimiters = arrayOf(" ", ",", ":", "\n", "\t", "-")
    }
}