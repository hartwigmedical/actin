package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.hartwig.actin.util.json.Json.array
import com.hartwig.actin.util.json.Json.bool
import com.hartwig.actin.util.json.Json.date
import com.hartwig.actin.util.json.Json.integer
import com.hartwig.actin.util.json.Json.nullableBool
import com.hartwig.actin.util.json.Json.nullableDate
import com.hartwig.actin.util.json.Json.objectNode
import com.hartwig.actin.util.json.Json.optionalArray
import com.hartwig.actin.util.json.Json.optionalBool
import com.hartwig.actin.util.json.Json.optionalObject
import com.hartwig.actin.util.json.Json.optionalString
import com.hartwig.actin.util.json.Json.optionalStringList
import com.hartwig.actin.util.json.Json.string
import com.hartwig.actin.util.json.Json.stringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class JsonTest {

    private val factory = JsonNodeFactory.instance

    @Test
    fun `Should extract objects`() {
        val obj = factory.objectNode()
        assertThat(optionalObject(obj, "object")).isNull()

        obj.set<com.fasterxml.jackson.databind.JsonNode>("object", factory.objectNode())
        assertThat(optionalObject(obj, "object")).isNotNull
        assertThat(objectNode(obj, "object")).isNotNull
    }

    @Test
    fun `Should extract arrays`() {
        val obj = factory.objectNode()
        assertThat(optionalArray(obj, "array1")).isNull()

        obj.set<com.fasterxml.jackson.databind.JsonNode>("array1", factory.arrayNode())
        assertThat(optionalArray(obj, "array1")).isNotNull
        assertThat(array(obj, "array1")).isNotNull
    }

    @Test
    fun `Should extract string lists`() {
        val obj = factory.objectNode()
        obj.putNull("nullable")
        assertThat(optionalStringList(obj, "array1")).isNull()

        val array = factory.arrayNode().add("value1").add("value2")
        obj.set<com.fasterxml.jackson.databind.JsonNode>("array1", array)
        assertThat(optionalStringList(obj, "array1")).hasSize(2)
        assertThat(stringList(obj, "array1")).hasSize(2)
    }

    @Test
    fun `Should treat single string as singleton list`() {
        val obj = factory.objectNode().put("scalar", "only")
        assertThat(stringList(obj, "scalar")).containsExactly("only")
    }

    @Test
    fun `Should extract strings`() {
        val obj = factory.objectNode()
        assertThat(optionalString(obj, "string")).isNull()

        obj.put("string", "value")
        assertThat(optionalString(obj, "string")).isEqualTo("value")
        assertThat(string(obj, "string")).isEqualTo("value")
    }

    @Test
    fun `Should extract integers`() {
        val obj = factory.objectNode().put("integer", 8)
        assertThat(integer(obj, "integer")).isEqualTo(8)
    }

    @Test
    fun `Should extract booleans`() {
        val obj = factory.objectNode()
        assertThat(optionalBool(obj, "bool")).isNull()
        obj.putNull("nullable")
        assertThat(nullableBool(obj, "nullable")).isNull()

        obj.put("bool", true)
        assertThat(nullableBool(obj, "bool")!!).isTrue
        assertThat(bool(obj, "bool")).isTrue
    }

    @Test
    fun `Should extract dates`() {
        val obj = factory.objectNode()
        obj.putNull("nullable")
        assertThat(nullableDate(obj, "nullable")).isNull()

        val dateNode = factory.objectNode().put("year", 2026).put("month", 6).put("day", 8)
        obj.set<com.fasterxml.jackson.databind.JsonNode>("date", dateNode)
        assertThat(date(obj, "date")).isEqualTo(LocalDate.of(2026, 6, 8))
    }
}
