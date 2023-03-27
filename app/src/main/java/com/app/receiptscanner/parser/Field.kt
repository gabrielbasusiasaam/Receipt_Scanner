package com.app.receiptscanner.parser

data class Field(val order: Int, val type: Int, val alias: String, var data: ArrayList<String>)
