package com.app.receiptscanner.parser


data class TokenRelation(
    val content: List<Regex>,
    val dataCount: Int,
    val flag: Int
) {
    class TokenRelationsBuilder {
        private val relations = arrayListOf<TokenRelation>()
        fun addRegexRelation(
            regex: String,
            dataCount: Int,
            checkLines: Int
        ): TokenRelationsBuilder {
            relations.add(TokenRelation(listOf(Regex(regex)), dataCount, checkLines))
            return this
        }

        fun addRegexRelation(
            regex: ArrayList<String>,
            dataCount: Int,
            checkLines: Int
        ): TokenRelationsBuilder {
            val regexMap = regex.map { Regex(it) }
            relations.add(TokenRelation(regexMap, dataCount, checkLines))
            return this
        }

        fun addKeyWordRelation(
            content: ArrayList<String>,
            dataCount: Int,
            checkLines: Int
        ): TokenRelationsBuilder {
            val contentMap = content.map { Regex(it.uppercase()) }
            relations.add(TokenRelation(contentMap, dataCount, checkLines))
            return this
        }

        fun addKeyWordRelation(
            content: String,
            dataCount: Int,
            checkLines: Int
        ): TokenRelationsBuilder {
            relations.add(TokenRelation(listOf(Regex(content.uppercase())), dataCount, checkLines))
            return this
        }

        fun clear(): TokenRelationsBuilder {
            relations.clear()
            return this
        }

        fun build(): ArrayList<TokenRelation> {
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