package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object Json {

    fun optionalObject(obj: JsonObject, field: String): JsonObject? {
        return if (obj.has(field)) `object`(obj, field) else null
    }

    fun `object`(obj: JsonObject, field: String): JsonObject {
        return obj.getAsJsonObject(field)
    }

    fun nullableArray(obj: JsonObject, field: String): JsonArray? {
        return if (!isNull(obj, field)) array(obj, field) else null
    }

    fun optionalArray(obj: JsonObject, field: String): JsonArray? {
        return if (obj.has(field)) array(obj, field) else null
    }

    fun array(obj: JsonObject, field: String): JsonArray {
        return obj.getAsJsonArray(field)
    }

    fun nullableStringList(obj: JsonObject, field: String): List<String>? {
        return if (!isNull(obj, field)) stringList(obj, field) else null
    }

    fun optionalStringList(obj: JsonObject, field: String): List<String>? {
        return if (obj.has(field)) stringList(obj, field) else null
    }

    fun stringList(obj: JsonObject, field: String): List<String> {
        return if (obj.get(field).isJsonPrimitive) {
            listOf(string(obj, field))
        } else {
            assert(obj.get(field).isJsonArray)
            obj.getAsJsonArray(field).map(JsonElement::getAsString)
        }
    }

    fun nullableString(obj: JsonObject, field: String): String? {
        return if (!isNull(obj, field)) string(obj, field) else null
    }

    fun optionalString(obj: JsonObject, field: String): String? {
        return if (obj.has(field)) string(obj, field) else null
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

    fun optionalBool(obj: JsonObject, field: String): Boolean? {
        return if (obj.has(field)) nullableBool(obj, field) else null
    }

    fun nullableBool(obj: JsonObject, field: String): Boolean? {
        return if (!isNull(obj, field)) bool(obj, field) else null
    }

    fun bool(obj: JsonObject, field: String): Boolean {
        return obj.get(field).asBoolean
    }

    private fun isNull(obj: JsonObject, field: String): Boolean {
        return obj.get(field).isJsonNull
    }
}
