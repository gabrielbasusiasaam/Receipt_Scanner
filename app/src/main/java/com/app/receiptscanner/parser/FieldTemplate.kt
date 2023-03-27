package com.app.receiptscanner.parser

import com.app.receiptscanner.database.Receipt
import com.app.receiptscanner.parser.FieldMap.Companion.FIELD_ITEM
import com.app.receiptscanner.storage.NormalizedReceipt

typealias NormalizedReceiptCallback = (receipts: List<NormalizedReceipt>) -> Unit
typealias ReceiptInsertionCallback = (receipt: Receipt) -> Unit

object FieldTemplate {
    const val MARKS_AND_SPENCERS_ID = 1 shl 0
    private val MARKS_AND_SPENCERS_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("Cost"), FIELD_ITEM, "Cost")
        .put(arrayListOf("Items"), FIELD_ITEM, "Items")
        .build()

    fun getFieldsById(id: Int) : FieldMap {
        val fields = hashMapOf(MARKS_AND_SPENCERS_ID to MARKS_AND_SPENCERS_FIELDS)
        return fields[id]!!
    }
}