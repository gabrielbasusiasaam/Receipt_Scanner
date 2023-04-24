package com.app.receiptscanner.storage

import com.app.receiptscanner.parser.FieldMap

/**
 * A data class condensing all information on a receipt into a set of standard fields, as well as
 * the fields specific to the specific type of receipt.
 */
data class NormalizedReceipt(
    var id: Int,
    var name: String,
    var dateCreated: Long,
    var photoPath: String?,
    val type: Int,
    val fields: FieldMap
)