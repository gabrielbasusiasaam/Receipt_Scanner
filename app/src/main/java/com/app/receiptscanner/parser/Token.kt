package com.app.receiptscanner.parser

data class Token(val type: Int, val content: ArrayList<String>, val lineNumber: Int, val relation: TokenRelation? = null) {
    companion object {
        const val TYPE_FIELD = 0
        const val TYPE_DATA = 1
    }
}