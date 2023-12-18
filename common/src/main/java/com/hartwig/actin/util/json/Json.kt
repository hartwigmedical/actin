package com.hartwig.actin.util.json

import com.google.common.collect.Lists
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.time.LocalDate
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

object Json {
    @JvmStatic
    fun optionalObject(`object`: JsonObject, field: String): JsonObject? {
        return if (`object`.has(field)) `object`(`object`, field) else null
    }

    @JvmStatic
    fun nullableObject(`object`: JsonObject, field: String): JsonObject? {
        return if (!isNull(`object`, field)) `object`(`object`, field) else null
    }

    @JvmStatic
    fun `object`(`object`: JsonObject, field: String): JsonObject {
        return `object`.getAsJsonObject(field)
    }

    @JvmStatic
    fun optionalArray(`object`: JsonObject, field: String): JsonArray? {
        return if (`object`.has(field)) array(`object`, field) else null
    }

    @JvmStatic
    fun nullableArray(`object`: JsonObject, field: String): JsonArray? {
        return if (!isNull(`object`, field)) array(`object`, field) else null
    }

    @JvmStatic
    fun array(`object`: JsonObject, field: String): JsonArray {
        return `object`.getAsJsonArray(field)
    }

    @JvmStatic
    fun optionalStringList(`object`: JsonObject, field: String): List<String>? {
        return if (`object`.has(field)) stringList(`object`, field) else null
    }

    @JvmStatic
    fun nullableStringList(`object`: JsonObject, field: String): List<String>? {
        return if (!isNull(`object`, field)) stringList(`object`, field) else null
    }

    @JvmStatic
    fun stringList(`object`: JsonObject, field: String): List<String> {
        val values: MutableList<String> = Lists.newArrayList()
        if (`object`.get(field).isJsonPrimitive()) {
            values.add(string(`object`, field))
        } else {
            assert(`object`.get(field).isJsonArray())
            for (element: JsonElement in `object`.getAsJsonArray(field)) {
                values.add(element.getAsJsonPrimitive().getAsString())
            }
        }
        return values
    }

    fun stringSet(`object`: JsonObject, field: String): Set<String> {
        return HashSet(stringList(`object`, field))
    }

    @JvmStatic
    fun nullableIntegerList(`object`: JsonObject, field: String): List<Int>? {
        return if (!isNull(`object`, field)) integerList(`object`, field) else null
    }

    @JvmStatic
    fun integerList(`object`: JsonObject, field: String): List<Int> {
        val values: MutableList<Int> = Lists.newArrayList()
        if (`object`.get(field).isJsonPrimitive()) {
            values.add(integer(`object`, field))
        } else {
            assert(`object`.get(field).isJsonArray())
            for (element: JsonElement in `object`.getAsJsonArray(field)) {
                values.add(element.getAsJsonPrimitive().getAsInt())
            }
        }
        return values
    }

    @JvmStatic
    fun optionalString(`object`: JsonObject, field: String): String? {
        return if (`object`.has(field)) string(`object`, field) else null
    }

    @JvmStatic
    fun nullableString(`object`: JsonObject, field: String): String? {
        return if (!isNull(`object`, field)) string(`object`, field) else null
    }

    @JvmStatic
    fun string(`object`: JsonObject, field: String): String {
        return `object`.get(field).getAsJsonPrimitive().getAsString()
    }

    @JvmStatic
    fun optionalNumber(`object`: JsonObject, field: String): Double? {
        return if (`object`.has(field)) nullableNumber(`object`, field) else null
    }

    @JvmStatic
    fun nullableNumber(`object`: JsonObject, field: String): Double? {
        return if (!isNull(`object`, field)) number(`object`, field) else null
    }

    @JvmStatic
    fun number(`object`: JsonObject, field: String): Double {
        return `object`.get(field).getAsJsonPrimitive().getAsDouble()
    }

    @JvmStatic
    fun nullableInteger(`object`: JsonObject, field: String): Int? {
        return if (!isNull(`object`, field)) integer(`object`, field) else null
    }

    @JvmStatic
    fun integer(`object`: JsonObject, field: String): Int {
        return `object`.get(field).getAsJsonPrimitive().getAsInt()
    }

    @JvmStatic
    fun optionalBool(`object`: JsonObject, field: String): Boolean? {
        return if (`object`.has(field)) nullableBool(`object`, field) else null
    }

    @JvmStatic
    fun nullableBool(`object`: JsonObject, field: String): Boolean? {
        return if (!isNull(`object`, field)) bool(`object`, field) else null
    }

    @JvmStatic
    fun bool(`object`: JsonObject, field: String): Boolean {
        return `object`.get(field).getAsJsonPrimitive().getAsBoolean()
    }

    @JvmStatic
    fun nullableDate(`object`: JsonObject, field: String): LocalDate? {
        return if (!isNull(`object`, field)) date(`object`, field) else null
    }

    @JvmStatic
    fun date(`object`: JsonObject, field: String): LocalDate {
        val dateObject: JsonObject = `object`(`object`, field)
        return LocalDate.of(integer(dateObject, "year"), integer(dateObject, "month"), integer(dateObject, "day"))
    }

    @JvmStatic
    fun <T> extractListFromJson(jsonArray: JsonArray, extractor: Function<JsonObject, T>): List<T> {
        return jsonArraytoObjectStream(jsonArray, extractor).collect(Collectors.toList())
    }

    @JvmStatic
    fun <T> extractSetFromJson(jsonArray: JsonArray, extractor: Function<JsonObject, T>): Set<T> {
        return jsonArraytoObjectStream(jsonArray, extractor).collect(Collectors.toSet())
    }

    private fun <T> jsonArraytoObjectStream(jsonArray: JsonArray, extractor: Function<JsonObject, T>): Stream<T> {
        return jsonArray.asList().stream().map(Function({ obj: JsonElement -> obj.getAsJsonObject() })).map(extractor)
    }

    private fun isNull(`object`: JsonObject, field: String): Boolean {
        return `object`.get(field).isJsonNull()
    }
}
