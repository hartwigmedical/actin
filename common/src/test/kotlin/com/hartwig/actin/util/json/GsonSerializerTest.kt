package com.hartwig.actin.util.json

import com.hartwig.actin.util.json.GsonSerializer.create
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GsonSerializerTest {

    @Test
    fun `Should create gson serializer`() {
        assertThat(create()).isNotNull()
    }
}