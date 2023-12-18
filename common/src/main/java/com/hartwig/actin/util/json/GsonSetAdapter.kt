package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Stream

class GsonSetAdapter<T>() : JsonSerializer<Set<T>> {
    public override fun serialize(set: Set<T>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonArray: JsonArray = JsonArray()
        var stream: Stream<T> = set.stream()
        if (!set.isEmpty() && set.iterator().next() is Comparable<*>) {
            stream = stream.sorted()
        }
        stream.map(Function({ o: T -> context.serialize(o) })).forEach(Consumer({ element: JsonElement? -> jsonArray.add(element) }))
        return jsonArray
    }
}
