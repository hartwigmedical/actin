package com.hartwig.actin.util.json

import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalDateAdapterTest {

    @Test
    fun `Should serialize and deserialize local date`() {
        val date = LocalDate.of(2022, 10, 20)
        val mapper = ActinObjectMapper.create()

        assertThat(mapper.writeValueAsString(date)).isEqualTo("\"2022-10-20\"")
        assertThat(mapper.readValue(mapper.writeValueAsString(date), LocalDate::class.java)).isEqualTo(date)
        assertThat(mapper.readValue(mapper.writeValueAsString(null as LocalDate?), LocalDate::class.java)).isNull()
    }
}
