package com.app.receiptscanner.storage

/**
 * A data class condensing all information on a receipt into a set of standard fields, as well as
 * the fields specific to the specific type of receipt.
 */
data class NormalizedReceipt(
    val name: String,
    val dateCreated: Long,
    val photoPath: String,
    val fields: HashMap<List<String>, String>
)