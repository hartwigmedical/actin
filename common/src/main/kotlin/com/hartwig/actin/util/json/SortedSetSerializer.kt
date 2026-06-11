package com.hartwig.actin.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

object SortedSetSerializer : JsonSerializer<Set<*>>() {

    private val NULL_SAFE_COMPARATOR: Comparator<Any?> = Comparator.nullsLast(Comparator(::compare))

    override fun serialize(value: Set<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartArray()
        value.sortedWith(NULL_SAFE_COMPARATOR).forEach { element ->
            if (element == null) {
                gen.writeNull()
            } else {
                serializers.defaultSerializeValue(element, gen)
            }
        }
        gen.writeEndArray()
    }

    private fun compare(a: Any, b: Any): Int {
        @Suppress("UNCHECKED_CAST")
        val comparable = a as? Comparable<Any>
        if (comparable != null) {
            try {
                return comparable.compareTo(b)
            } catch (_: ClassCastException) {}
        }
        return a.toString().compareTo(b.toString())
    }
}
