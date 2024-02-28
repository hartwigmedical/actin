package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class GsonSetAdapter<T> : JsonSerializer<Set<T>> {

    override fun serialize(set: Set<T>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonArray = JsonArray()
        set.sortedWith(Comparator.nullsLast(::compare)).map { context.serialize(it) }.forEach(jsonArray::add)
        return jsonArray
    }

    fun <T> compare(a: T, b: T): Int {
        return try {
            (a as? Comparable<T>)?.compareTo(b) ?: compareAsStrings(a, b)
        } catch (e: ClassCastException) {
            compareAsStrings(a, b)
        }
    }

    private fun <T> compareAsStrings(a: T, b: T) = a.toString().compareTo(b.toString())
}