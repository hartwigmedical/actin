package com.hartwig.actin.util.json

import com.google.gson.GsonBuilder
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GsonLocalDateAdapterTest {

    @Test
    fun `Should serialize local date`() {
        val date = LocalDate.of(2022, 10, 20)
        val serializer = GsonBuilder().serializeNulls()
            .registerTypeAdapter(LocalDate::class.java, GsonLocalDateAdapter())
            .create()
        assertThat(serializer.fromJson(serializer.toJson(date), LocalDate::class.java)).isEqualTo(date)
        assertThat(serializer.fromJson(serializer.toJson(null as LocalDate?), LocalDate::class.java)).isNull()
    }
}