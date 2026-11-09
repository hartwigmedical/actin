package com.hartwig.actin.util.json

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ComplexKeyMapModuleTest {

    private val mapper = ActinObjectMapper.create().registerModule(ComplexKeyMapModule())

    @Test
    fun `Should serialize empty complex key map as empty object`() {
        val serialized = mapper.writeValueAsString(Wrapper(emptyMap()))

        assertThat(serialized).isEqualTo("""{"map":{}}""")
        assertThat(mapper.readValue(serialized, Wrapper::class.java)).isEqualTo(Wrapper(emptyMap()))
    }

    @Test
    fun `Should serialize complex key map as array of key value pairs`() {
        val wrapper = Wrapper(mapOf(Key("a", 1) to "x"))
        val serialized = mapper.writeValueAsString(wrapper)

        assertThat(serialized).isEqualTo("""{"map":[[{"name":"a","rank":1},"x"]]}""")
        assertThat(mapper.readValue(serialized, Wrapper::class.java)).isEqualTo(wrapper)
    }

    private data class Key(val name: String, val rank: Int)

    private data class Wrapper(val map: Map<Key, String>)
}
