package com.hartwig.actin.util

import com.hartwig.actin.util.ResourceFile.bool
import com.hartwig.actin.util.ResourceFile.optionalBool
import com.hartwig.actin.util.ResourceFile.optionalDate
import com.hartwig.actin.util.ResourceFile.optionalInteger
import com.hartwig.actin.util.ResourceFile.optionalNumber
import com.hartwig.actin.util.ResourceFile.optionalString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.time.LocalDate

class ResourceFileTest {

    private val epsilon = 1.0E-10

    @Test
    fun `Should parse strings`() {
        assertThat(optionalString("")).isNull()
        assertThat(optionalString("hi")).isEqualTo("hi")
    }

    @Test
    fun `Should parse booleans`() {
        assertThat(optionalBool("unknown")).isNull()
        assertThat(optionalBool("")).isNull()
        assertThat(optionalBool("1")!!).isTrue
        assertThat(optionalBool("0")!!).isFalse
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should crash on invalid boolean`() {
        bool("True")
    }

    @Test
    fun `Should parse dates`() {
        assertThat(optionalDate("")).isNull()
        assertThat(optionalDate("2019-04-20")).isEqualTo(LocalDate.of(2019, 4, 20))
    }

    @Test
    fun `Should parse integers`() {
        assertThat(optionalInteger("")).isNull()
        assertThat((optionalInteger("4") as Int).toLong()).isEqualTo(4)
    }

    @Test
    fun `Should parse doubles`() {
        assertThat(optionalNumber("")).isNull()
        assertThat(optionalNumber("4.2")!!).isEqualTo(4.2, Offset.offset(epsilon))
    }
}