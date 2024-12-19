package com.hartwig.actin.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MapFunctionsTest {

    @Test
    fun `Should merge actual map of sets`() {
        val map1 = mapOf("key 1" to setOf("value 1", "value 2"))
        val map2 = mapOf("key 1" to setOf("value 2", "value 3"))
        val map3 = mapOf("key 2" to setOf("value 1", "value 3"))

        val result = MapFunctions.mergeMapsOfSets(mapsOfSets = listOf(map1, map2, map3))

        assertThat(result).isEqualTo(
            mapOf(
                "key 1" to setOf("value 1", "value 2", "value 3"),
                "key 2" to setOf("value 1", "value 3")
            )
        )
    }
}