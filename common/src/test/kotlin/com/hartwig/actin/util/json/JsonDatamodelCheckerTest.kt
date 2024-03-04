package com.hartwig.actin.util.json

import com.google.gson.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonDatamodelCheckerTest {

    private val datamodel = mapOf(
        "A" to true,
        "B" to false
    )
    private val checker = JsonDatamodelChecker("test", datamodel)
    
    @Test
    fun `Should fail for missing required field`() {
        val obj = JsonObject()
        assertThat(checker.check(obj)).isFalse
    }

    @Test
    fun `Should pass when all required fields present`() {
        val obj = JsonObject()
        obj.addProperty("A", "test A")
        assertThat(checker.check(obj)).isTrue
    }

    @Test
    fun `Should pass when required fields and optional fields present`() {
        val obj = JsonObject()
        obj.addProperty("A", "test A")
        obj.addProperty("B", "test B")
        assertThat(checker.check(obj)).isTrue
    }

    @Test
    fun `Should fail when unexpected field present`() {
        val obj = JsonObject()
        obj.addProperty("A", "test A")
        obj.addProperty("B", "test B")
        obj.addProperty("C", "test C")
        assertThat(checker.check(obj)).isFalse
    }
}