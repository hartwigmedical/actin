package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class GsonSetAdapter<T : Comparable<T>> : JsonSerializer<Set<T>> {

    override fun serialize(set: Set<T>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonArray = JsonArray()
        set.sorted().map { context.serialize(it) }.forEach(jsonArray::add)
        return jsonArray
    }
}
