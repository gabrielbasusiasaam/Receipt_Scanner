package com.app.receiptscanner.parser

import android.util.Log


class FieldMap : HashMap<List<String>, Field>() {
    var costField = arrayListOf<String>()

    @Suppress("UNCHECKED_CAST")
    override fun clone(): FieldMap {
        val cloneTotalField = costField.clone() as ArrayList<String>
        val cloneMap = FieldMap()
        forEach {
            val key = it.key.toMutableList()
            cloneMap[key] = it.value.copy()
        }
        cloneMap.costField = cloneTotalField
        return cloneMap
    }

    fun setField(key: List<String>, value: ArrayList<String>) {
        Log.e("KEY HASH", key.hashCode().toString())
        this[key]?.data = value
    }

    fun get(position: Int): Pair<List<String>, Field> {
        val values = toList().sortedBy { it.second.order }
        return values[position]
    }

    fun getCost(): ArrayList<String> {
        return this[costField]!!.data
    }

    class Builder {
        private val map = FieldMap()
        private var costField = arrayListOf<String>()
        private var currentPosition: Int = 0
        fun put(
            keyWords: ArrayList<String>,
            type: Int,
            alias: String,
            dataCount: Int,
            directionFlags: Int,
            isCostField: Boolean = false,
        ): Builder {

            val field = Field(
                data = arrayListOf(),
                alias = alias,
                dataCount = dataCount,
                direction = directionFlags,
                type = type,
                order = currentPosition
            )
            currentPosition += 1
            map[keyWords] = field
            if (isCostField) costField = ArrayList(keyWords)
            return this
        }

        fun build(): FieldMap {
            assert(costField.isNotEmpty()) { "The field storing the total must be set" }
            map.costField = costField
            return map
        }
    }

    companion object {
        const val FIELD_ITEM = 1 shl 0
        const val FIELD_LIST = 1 shl 1
    }
}
