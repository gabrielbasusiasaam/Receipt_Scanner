package com.app.receiptscanner.parser

data class Field(
    var data: ArrayList<String>,
    val alias: String,
    val type: Int,
    val dataCount: Int,
    val direction: Int,
    val order: Int,
)
