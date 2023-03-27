package com.app.receiptscanner.parser

class FieldMap(private val map: HashMap<ArrayList<String>, Field> = hashMapOf()) {

    fun set(field: ArrayList<String>, data: ArrayList<String>) {
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
        private var currentPosition: Int = 0
        fun put(field: ArrayList<String>, type: Int, alias: String): Builder {
            map[field] = Field(currentPosition, type, alias, arrayListOf())
            return this
        }

        fun build(): FieldMap {
            return FieldMap(map)
        }
    }

    companion object {
        const val FIELD_ITEM = 1 shl 0
        const val FIELD_LIST = 1 shl 1
    }
}
