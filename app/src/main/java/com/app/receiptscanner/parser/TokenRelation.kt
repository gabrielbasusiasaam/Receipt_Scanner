package com.app.receiptscanner.parser

data class TokenRelation(
    val content: ArrayList<String>,
    val fieldCount: Int,
    val dataCount: Int,
    val checkLines: Int
) {
    class TokenRelationsBuilder {
        private val relations = arrayListOf<TokenRelation>()
        fun addTokenRelation(content: String, dataCount: Int, checkLines: Int): TokenRelationsBuilder {
            relations.add(TokenRelation(arrayListOf(content), 1, dataCount, checkLines))
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
        const val LINE_CURRENT = 0b00000001
        const val LINE_BELOW = 0b00000010
        const val LINE_ABOVE = 0b00000100
    }
}