package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.time.LocalDate

object Json {

    fun optionalObject(obj: JsonObject, field: String): JsonObject? {
        return if (obj.has(field)) nullableObject(obj, field) else null
    }

    fun nullableObject(obj: JsonObject, field: String): JsonObject? {
        return if (!isNull(obj, field)) `object`(obj, field) else null
    }

    fun `object`(obj: JsonObject, field: String): JsonObject {
        return obj.getAsJsonObject(field)
    }

    fun optionalArray(obj: JsonObject, field: String): JsonArray? {
        return if (obj.has(field)) nullableArray(obj, field) else null
    }

    fun nullableArray(obj: JsonObject, field: String): JsonArray? {
        return if (!isNull(obj, field)) array(obj, field) else null
    }

    fun array(obj: JsonObject, field: String): JsonArray {
        return obj.getAsJsonArray(field)
    }

    fun optionalStringList(obj: JsonObject, field: String): List<String>? {
        return if (obj.has(field)) nullableStringList(obj, field) else null
    }

    fun nullableStringList(obj: JsonObject, field: String): List<String>? {
        return if (!isNull(obj, field)) stringList(obj, field) else null
    }

    fun stringList(obj: JsonObject, field: String): List<String> {
        return if (obj.get(field).isJsonPrimitive) {
            listOf(string(obj, field))
        } else {
            assert(obj.get(field).isJsonArray)
            obj.getAsJsonArray(field).map(JsonElement::getAsString)
        }
    }

    fun optionalString(obj: JsonObject, field: String): String? {
        return if (obj.has(field)) nullableString(obj, field) else null
    }

    fun nullableString(obj: JsonObject, field: String): String? {
        return if (!isNull(obj, field)) string(obj, field) else null
    }

    fun string(obj: JsonObject, field: String): String {
        return obj.get(field).asString
    }

    fun nullableInteger(obj: JsonObject, field: String): Int? {
        return if (!isNull(obj, field)) integer(obj, field) else null
    }

    fun integer(obj: JsonObject, field: String): Int {
        return obj.get(field).asInt
    }

    fun optionalDouble(obj: JsonObject, field: String): Double? {
        return if (obj.has(field)) nullableDouble(obj, field) else null
    }

    fun nullableDouble(obj: JsonObject, field: String): Double? {
        return if (!isNull(obj, field)) double(obj, field) else null
    }

    fun double(obj: JsonObject, field: String): Double {
        return obj.get(field).asDouble
    }

    fun optionalBool(obj: JsonObject, field: String): Boolean? {
        return if (obj.has(field)) nullableBool(obj, field) else null
    }

    fun nullableBool(obj: JsonObject, field: String): Boolean? {
        return if (!isNull(obj, field)) bool(obj, field) else null
    }

    fun bool(obj: JsonObject, field: String): Boolean {
        return obj.get(field).asBoolean
    }

    fun nullableDate(obj: JsonObject, field: String): LocalDate? {
        return if (!isNull(obj, field)) date(obj, field) else null
    }

    fun date(obj: JsonObject, field: String): LocalDate {
        val jsonDate: JsonObject = `object`(obj, field)
        return LocalDate.of(integer(jsonDate, "year"), integer(jsonDate, "month"), integer(jsonDate, "day"))
    }

    private fun isNull(obj: JsonObject, field: String): Boolean {
        return obj.get(field).isJsonNull
    }
}
