package com.hartwig.actin.util.json

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class GsonLocalDateTimeAdapterTest {

    @Test
    fun `Should serialize LocalDateTime object`() {
        val dateTime = LocalDateTime.of(2022, 10, 20, 11, 12, 13)
        val serializer = GsonSerializer.create()
        assertThat(serializer.fromJson(serializer.toJson(dateTime), LocalDateTime::class.java)).isEqualTo(dateTime)
        assertThat(serializer.fromJson(serializer.toJson(null as LocalDateTime?), LocalDateTime::class.java)).isNull()
    }
}