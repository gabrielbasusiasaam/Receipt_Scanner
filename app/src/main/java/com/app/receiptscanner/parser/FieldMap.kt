package com.app.receiptscanner.parser

import android.util.Log


class FieldMap(private val map: HashMap<ArrayList<String>, Field> = hashMapOf()) {
    var totalField = arrayListOf<String>()

    @Suppress("UNCHECKED_CAST")
    fun clone(): FieldMap {
        val cloneTotalField = totalField.clone() as ArrayList<String>
        val cloneMap = HashMap<ArrayList<String>, Field>()
        map.forEach {
            val key = it.key.clone() as ArrayList<String>
            cloneMap[key] = it.value.copy()
        }
        val cloneFieldMap = FieldMap(cloneMap)
        cloneFieldMap.totalField = cloneTotalField
        return cloneFieldMap
    }

    fun set(field: ArrayList<String>, data: ArrayList<String>) {
        assert(map.containsKey(field))
        Log.e("FIELD", "$data")
        map[field]?.data = data
    }

    fun get(field: ArrayList<String>): Field? {
        return map[field]
    }

    fun get(position: Int): Pair<java.util.ArrayList<String>, Field> {
        val values = map.toList().sortedBy { it.second.order }
        return values[position]
    }

    fun getMap(): HashMap<ArrayList<String>, Field> {
        return map
    }

    fun clear(field: ArrayList<String>) {
        map[field]?.data?.clear()
    }

    fun clearAll() {
        map.forEach { it.value.data.clear() }
    }

    class Builder {
        private val map = hashMapOf<ArrayList<String>, Field>()
        private var _totalField = arrayListOf<String>()
        private var currentPosition: Int = 0
        fun put(
            field: ArrayList<String>,
            type: Int,
            alias: String,
            isCostField: Boolean = false
        ): Builder {
            map[field] = Field(currentPosition, type, alias, arrayListOf())
            if (isCostField) _totalField = field
            return this
        }

        fun build(): FieldMap {
            assert(_totalField.isNotEmpty()) { "The field storing the total must be set" }
            return FieldMap(map).apply {
                totalField = _totalField
            }
        }
    }

    companion object {
        const val FIELD_ITEM = 1 shl 0
        const val FIELD_LIST = 1 shl 1
    }
}
