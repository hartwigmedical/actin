package com.hartwig.actin.util.json

import com.hartwig.actin.util.json.GsonSerializer.create
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class GsonLocalDateAdapterTest {
    @Test
    fun canSerializeLocalDate() {
        val date = LocalDate.of(2022, 10, 20)
        val serializer = create()
        Assert.assertEquals(date, serializer.fromJson(serializer.toJson(date), LocalDate::class.java))
        Assert.assertNull(serializer.fromJson(serializer.toJson(null as LocalDate?), LocalDate::class.java))
    }
}