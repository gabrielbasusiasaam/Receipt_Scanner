package com.app.receiptscanner.parser

data class TokenField(
    val content: List<Regex>,
    val dataCount: Int,
    val flag: Int
) {
    class TokenFieldBuilder {
        private val relations = arrayListOf<TokenField>()
        fun addRegexRelation(
            regex: String,
            dataCount: Int,
            checkLines: Int
        ): TokenFieldBuilder {
            relations.add(TokenField(listOf(Regex(regex)), dataCount, checkLines))
            return this
        }

        fun addRegexRelation(
            regex: ArrayList<String>,
            dataCount: Int,
            checkLines: Int
        ): TokenFieldBuilder {
            val regexMap = regex.map { Regex(it) }
            relations.add(TokenField(regexMap, dataCount, checkLines))
            return this
        }

        fun addKeyWordRelation(
            content: ArrayList<String>,
            dataCount: Int,
            checkLines: Int
        ): TokenFieldBuilder {
            val contentMap = content.map { Regex(it.uppercase()) }
            relations.add(TokenField(contentMap, dataCount, checkLines))
            return this
        }

        fun addKeyWordRelation(
            content: String,
            dataCount: Int,
            checkLines: Int
        ): TokenFieldBuilder {
            relations.add(TokenField(listOf(Regex(content.uppercase())), dataCount, checkLines))
            return this
        }

        fun clear(): TokenFieldBuilder {
            relations.clear()
            return this
        }

        fun build(): ArrayList<TokenField> {
            return relations
        }
    }

    companion object {
        //Flags
        const val CHECK_AFTER = 1 shl 0
        const val CHECK_BEFORE = 1 shl 1
        const val CHECK_ABOVE = 1 shl 2
        const val CHECK_BELOW = 1 shl 3

    }
}