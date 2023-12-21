package com.hartwig.actin.util.json

import com.hartwig.actin.util.json.GsonSerializer.create
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class GsonLocalDateAdapterTest {

    @Test
    fun `Should serialize local date`() {
        val date = LocalDate.of(2022, 10, 20)
        val serializer = create()
        assertThat(serializer.fromJson(serializer.toJson(date), LocalDate::class.java)).isEqualTo(date)
        assertThat(serializer.fromJson(serializer.toJson(null as LocalDate?), LocalDate::class.java)).isNull()
    }
}