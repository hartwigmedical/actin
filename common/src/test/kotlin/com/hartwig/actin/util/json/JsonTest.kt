package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.hartwig.actin.util.json.Json.array
import com.hartwig.actin.util.json.Json.bool
import com.hartwig.actin.util.json.Json.date
import com.hartwig.actin.util.json.Json.extractListFromJson
import com.hartwig.actin.util.json.Json.extractSetFromJson
import com.hartwig.actin.util.json.Json.integer
import com.hartwig.actin.util.json.Json.integerList
import com.hartwig.actin.util.json.Json.nullableArray
import com.hartwig.actin.util.json.Json.nullableBool
import com.hartwig.actin.util.json.Json.nullableDate
import com.hartwig.actin.util.json.Json.nullableInteger
import com.hartwig.actin.util.json.Json.nullableIntegerList
import com.hartwig.actin.util.json.Json.nullableNumber
import com.hartwig.actin.util.json.Json.nullableObject
import com.hartwig.actin.util.json.Json.nullableString
import com.hartwig.actin.util.json.Json.nullableStringList
import com.hartwig.actin.util.json.Json.number
import com.hartwig.actin.util.json.Json.`object`
import com.hartwig.actin.util.json.Json.optionalArray
import com.hartwig.actin.util.json.Json.optionalBool
import com.hartwig.actin.util.json.Json.optionalNumber
import com.hartwig.actin.util.json.Json.optionalObject
import com.hartwig.actin.util.json.Json.optionalString
import com.hartwig.actin.util.json.Json.optionalStringList
import com.hartwig.actin.util.json.Json.string
import com.hartwig.actin.util.json.Json.stringList
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class JsonTest {
    @Test
    fun canExtractObjects() {
        val `object` = JsonObject()
        Assert.assertNull(optionalObject(`object`, "object"))
        `object`.add("object", JsonObject())
        Assert.assertNotNull(optionalObject(`object`, "object"))
        Assert.assertNotNull(`object`(`object`, "object"))
        `object`.add("null", null)
        Assert.assertNull(nullableObject(`object`, "null"))
    }

    @Test
    fun canExtractArrays() {
        val `object` = JsonObject()
        Assert.assertNull(optionalArray(`object`, "array1"))
        `object`.add("array1", JsonArray())
        Assert.assertNotNull(nullableArray(`object`, "array1"))
        Assert.assertNotNull(optionalArray(`object`, "array1"))
        Assert.assertNotNull(array(`object`, "array1"))
        `object`.add("array2", JsonNull.INSTANCE)
        Assert.assertNull(nullableArray(`object`, "array2"))
    }

    @Test
    fun canExtractStringLists() {
        val `object` = JsonObject()
        `object`.addProperty("nullable", null as String?)
        Assert.assertNull(optionalStringList(`object`, "array1"))
        Assert.assertNull(nullableStringList(`object`, "nullable"))
        val array = JsonArray()
        array.add("value1")
        array.add("value2")
        `object`.add("array1", array)
        Assert.assertEquals(2, optionalStringList(`object`, "array1")!!.size.toLong())
        Assert.assertEquals(2, nullableStringList(`object`, "array1")!!.size.toLong())
        Assert.assertEquals(2, stringList(`object`, "array1").size.toLong())
        `object`.addProperty("string", "string")
        Assert.assertEquals(1, nullableStringList(`object`, "string")!!.size.toLong())
    }

    @Test
    fun canExtractIntegerLists() {
        val `object` = JsonObject()
        `object`.addProperty("nullable", null as Int?)
        Assert.assertNull(nullableIntegerList(`object`, "nullable"))
        val array = JsonArray()
        array.add(1)
        array.add(2)
        `object`.add("array1", array)
        Assert.assertEquals(2, nullableIntegerList(`object`, "array1")!!.size.toLong())
        Assert.assertEquals(2, integerList(`object`, "array1").size.toLong())
        `object`.addProperty("integer", 1)
        Assert.assertEquals(1, nullableIntegerList(`object`, "integer")!!.size.toLong())
    }

    @Test
    fun canExtractStrings() {
        val `object` = JsonObject()
        Assert.assertNull(optionalString(`object`, "string"))
        `object`.addProperty("string", "value")
        Assert.assertEquals("value", optionalString(`object`, "string"))
        Assert.assertEquals("value", nullableString(`object`, "string"))
        Assert.assertEquals("value", string(`object`, "string"))
        `object`.addProperty("nullable", null as String?)
        Assert.assertNull(nullableString(`object`, "nullable"))
    }

    @Test
    fun canExtractNumbers() {
        val `object` = JsonObject()
        Assert.assertNull(optionalNumber(`object`, "number"))
        `object`.addProperty("nullable", null as String?)
        Assert.assertNull(nullableNumber(`object`, "nullable"))
        `object`.addProperty("number", 12.4)
        Assert.assertEquals(12.4, nullableNumber(`object`, "number")!!, EPSILON)
        Assert.assertEquals(12.4, number(`object`, "number"), EPSILON)
    }

    @Test
    fun canExtractIntegers() {
        val `object` = JsonObject()
        `object`.addProperty("nullable", null as String?)
        Assert.assertNull(nullableInteger(`object`, "nullable"))
        `object`.addProperty("integer", 8)
        Assert.assertEquals(8, (nullableInteger(`object`, "integer") as Int).toLong())
        Assert.assertEquals(8, integer(`object`, "integer").toLong())
    }

    @Test
    fun canExtractBooleans() {
        val `object` = JsonObject()
        Assert.assertNull(optionalBool(`object`, "bool"))
        `object`.addProperty("nullable", null as String?)
        Assert.assertNull(nullableBool(`object`, "nullable"))
        `object`.addProperty("bool", true)
        Assert.assertTrue(nullableBool(`object`, "bool")!!)
        Assert.assertTrue(bool(`object`, "bool"))
    }

    @Test
    fun canExtractDates() {
        val `object` = JsonObject()
        `object`.addProperty("nullable", null as String?)
        Assert.assertNull(nullableDate(`object`, "nullable"))
        val dateObject = JsonObject()
        dateObject.addProperty("year", 2018)
        dateObject.addProperty("month", 4)
        dateObject.addProperty("day", 6)
        `object`.add("date", dateObject)
        Assert.assertEquals(LocalDate.of(2018, 4, 6), nullableDate(`object`, "date"))
        Assert.assertEquals(LocalDate.of(2018, 4, 6), date(`object`, "date"))
    }

    @Test
    fun shouldExtractEmptyJsonArrayToEmptyList() {
        Assert.assertEquals(emptyList<Any>(), extractListFromJson(JsonArray()) { jsonObject: JsonObject -> getIndex(jsonObject) })
    }

    @Test
    fun shouldExtractJsonArrayToList() {
        val list = listOf(3, 2, 1)
        Assert.assertEquals(list, extractListFromJson(jsonArrayFromCollection(list)) { jsonObject: JsonObject -> getIndex(jsonObject) })
    }

    @Test
    fun shouldExtractEmptyJsonArrayToEmptySet() {
        Assert.assertEquals(emptySet<Any>(), extractSetFromJson(JsonArray()) { jsonObject: JsonObject -> getIndex(jsonObject) })
    }

    @Test
    fun shouldExtractJsonArrayToSet() {
        val set = setOf(3, 2, 1)
        Assert.assertEquals(set, extractSetFromJson(jsonArrayFromCollection(set)) { jsonObject: JsonObject -> getIndex(jsonObject) })
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private fun jsonArrayFromCollection(collection: Collection<Int>): JsonArray {
            val jsonArray = JsonArray()
            collection.stream().map { i: Int? ->
                val jsonObject = JsonObject()
                jsonObject.addProperty("index", i)
                jsonObject
            }.forEach { element: JsonObject? -> jsonArray.add(element) }
            return jsonArray
        }

        private fun getIndex(jsonObject: JsonObject): Int {
            return jsonObject["index"].asInt
        }
    }
}