package com.hartwig.actin.util.json

import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalDateTimeAdapterTest {

    @Test
    fun `Should serialize and deserialize LocalDateTime`() {
        val dateTime = LocalDateTime.of(2022, 10, 20, 11, 12, 13)
        val mapper = ActinObjectMapper.create()

        assertThat(mapper.readValue(mapper.writeValueAsString(dateTime), LocalDateTime::class.java)).isEqualTo(dateTime)
        assertThat(mapper.readValue(mapper.writeValueAsString(null as LocalDateTime?), LocalDateTime::class.java)).isNull()
    }
}
