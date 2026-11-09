package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonDatamodelCheckerTest {

    private val datamodel = mapOf(
        "A" to true,
        "B" to false
    )
    private val checker = JsonDatamodelChecker("test", datamodel)
    private val factory = JsonNodeFactory.instance

    @Test
    fun `Should fail for missing required field`() {
        assertThat(checker.check(factory.objectNode())).isFalse
    }

    @Test
    fun `Should pass when all required fields present`() {
        val obj = factory.objectNode().put("A", "test A")
        assertThat(checker.check(obj)).isTrue
    }

    @Test
    fun `Should pass when required and optional fields present`() {
        val obj = factory.objectNode().put("A", "test A").put("B", "test B")
        assertThat(checker.check(obj)).isTrue
    }

    @Test
    fun `Should fail when unexpected field present`() {
        val obj = factory.objectNode().put("A", "test A").put("B", "test B").put("C", "test C")
        assertThat(checker.check(obj)).isFalse
    }
}
