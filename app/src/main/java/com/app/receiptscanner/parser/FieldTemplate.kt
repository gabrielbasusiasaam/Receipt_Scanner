package com.app.receiptscanner.parser

import com.app.receiptscanner.parser.FieldMap.Companion.FIELD_ITEM
import com.app.receiptscanner.parser.FieldMap.Companion.FIELD_LIST

/**
 * An object storing templates of the necessary fields for all of the supported receipt types.
 * These templates contain the key words / patterns that the parser should look for when tokenizing,
 * the type of the value stored in that field, an alias to show the user, as well as a flag
 * indicating whether it is the field storing the total price of the receipt
 *
 * @see Field
 * @see FieldMap
 */
object FieldTemplate {
    const val MARKS_AND_SPENCERS_ID = 0
    const val MORRISONS_ID = 1
    const val LIDL_ID = 2
    const val SAINSBURYS_ID = 3
    const val WAITROSE_ID = 4

    private val MARKS_AND_SPENCERS_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("Cost"), FIELD_ITEM, "Cost", true)
        .put(arrayListOf("Items"), FIELD_ITEM, "Items")
        .build()

    private val MORRISONS_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("Description"), FIELD_LIST, "Items")
        .put(arrayListOf("Balance", "Due"), FIELD_ITEM, "Cost", true)
        .build()


    private val WAITROSE_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("Balance", "Due"), FIELD_ITEM, "Cost", true)
        .build()


    private val LIDL_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("Total"), FIELD_ITEM, "Total", true)
        .build()


    /**
     * Returns the field template associated with a specific template id. The object is cloned
     * before returning to avoid the fields being affected globally.
     *
     * If the template id passed does not exist, this function will through a null pointer exception
     *
     * @param id The template id for the required receipt type
     * @return A FieldMap containing all the required fields for the receipt type
     */
    fun getFieldsById(id: Int): FieldMap {
        val fields = hashMapOf(
            MARKS_AND_SPENCERS_ID to MARKS_AND_SPENCERS_FIELDS,
            MORRISONS_ID to MORRISONS_FIELDS,
            WAITROSE_ID to WAITROSE_FIELDS,
            LIDL_ID to LIDL_FIELDS
        )
        return fields[id]!!.clone()
    }
}