package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hartwig.actin.util.json.Json.array
import com.hartwig.actin.util.json.Json.bool
import com.hartwig.actin.util.json.Json.integer
import com.hartwig.actin.util.json.Json.nullableBool
import com.hartwig.actin.util.json.Json.`object`
import com.hartwig.actin.util.json.Json.optionalArray
import com.hartwig.actin.util.json.Json.optionalBool
import com.hartwig.actin.util.json.Json.optionalObject
import com.hartwig.actin.util.json.Json.optionalString
import com.hartwig.actin.util.json.Json.optionalStringList
import com.hartwig.actin.util.json.Json.string
import com.hartwig.actin.util.json.Json.stringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonTest {

    @Test
    fun `Should extract objects`() {
        val obj = JsonObject()
        assertThat(optionalObject(obj, "object")).isNull()

        obj.add("object", JsonObject())
        assertThat(optionalObject(obj, "object")).isNotNull
        assertThat(`object`(obj, "object")).isNotNull
    }

    @Test
    fun `Should extract arrays`() {
        val obj = JsonObject()
        assertThat(optionalArray(obj, "array1")).isNull()

        obj.add("array1", JsonArray())
        assertThat(optionalArray(obj, "array1")).isNotNull
        assertThat(array(obj, "array1")).isNotNull
    }

    @Test
    fun `Should extract string lists`() {
        val obj = JsonObject()
        obj.addProperty("nullable", null as String?)
        assertThat(optionalStringList(obj, "array1")).isNull()
        
        val array = JsonArray()
        array.add("value1")
        array.add("value2")
        obj.add("array1", array)
        assertThat(optionalStringList(obj, "array1")).hasSize(2)
        assertThat(stringList(obj, "array1")).hasSize(2)
    }

    @Test
    fun `Should extract strings`() {
        val obj = JsonObject()
        assertThat(optionalString(obj, "string")).isNull()

        obj.addProperty("string", "value")
        assertThat(optionalString(obj, "string")).isEqualTo("value")
        assertThat(string(obj, "string")).isEqualTo("value")
    }

    @Test
    fun `Should extract integers`() {
        val obj = JsonObject()
        obj.addProperty("integer", 8)
        assertThat(integer(obj, "integer")).isEqualTo(8)
    }

    @Test
    fun `Should extract booleans`() {
        val obj = JsonObject()
        assertThat(optionalBool(obj, "bool")).isNull()
        obj.addProperty("nullable", null as String?)
        assertThat(nullableBool(obj, "nullable")).isNull()

        obj.addProperty("bool", true)
        assertThat(nullableBool(obj, "bool")!!).isTrue
        assertThat(bool(obj, "bool")).isTrue
    }
}