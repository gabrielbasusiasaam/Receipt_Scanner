package com.app.receiptscanner.parser

data class TokenRelationship(
    val token: Token,
    var above: TokenRelationship? = null,
    var below: TokenRelationship? = null,
    var left: TokenRelationship? = null,
    var right: TokenRelationship? = null
) {
    override fun toString(): String {
        return "[token=$token, above=${above?.token}, below=${below?.token}, left=${left?.token}, right=${right?.token}]"
    }
}