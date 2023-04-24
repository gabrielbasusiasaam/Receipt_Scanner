package com.app.receiptscanner.parser

import android.graphics.Rect

data class Token(
    val type: Int,
    val content: ArrayList<String>,
    val lineNumber: Int,
    val boundingBox: Rect,
    val field: Field? = null,
    val regex: List<String>? = null
) {
    companion object {
        const val TYPE_FIELD = 0
        const val TYPE_DATA = 1
    }

    override fun toString(): String {
        return "content=$content"
    }
}