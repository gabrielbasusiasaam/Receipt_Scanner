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
    const val CHECK_AFTER = 1 shl 0
    const val CHECK_BEFORE = 1 shl 1
    const val CHECK_ABOVE = 1 shl 2
    const val CHECK_BELOW = 1 shl 3

    private val MARKS_AND_SPENCERS_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("COST"), FIELD_ITEM, "Cost", 1, CHECK_AFTER, true)
        .put(arrayListOf("ITEMS"), FIELD_LIST, "Items", -1, CHECK_AFTER)
        .build()

    private val MORRISONS_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("DESCRIPTION"), FIELD_LIST, "Items", 1, CHECK_AFTER)
        .put(arrayListOf("BALANCE", "DUE"), FIELD_ITEM, "Cost", 1, CHECK_AFTER, true)
        .build()


    private val WAITROSE_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("BALANCE", "DUE"), FIELD_ITEM, "Cost", 1, CHECK_AFTER, true)
        .build()


    private val LIDL_FIELDS = FieldMap
        .Builder()
        .put(arrayListOf("TOTAL"), FIELD_ITEM, "Total", 1, CHECK_AFTER, true)
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