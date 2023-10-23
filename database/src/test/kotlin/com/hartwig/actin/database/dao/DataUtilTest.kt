package com.hartwig.actin.database.dao

import com.hartwig.actin.database.dao.DataUtil.concat
import com.hartwig.actin.database.dao.DataUtil.nullableToString
import com.hartwig.actin.database.dao.DataUtil.toByte
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DataUtilTest {
    @Test
    fun shouldConvertToByte() {
        assertThat(toByte(true)).isEqualTo(1.toByte())
        assertThat(toByte(false)).isEqualTo(0.toByte())
        assertThat(toByte(null)).isNull()
    }

    @Test
    fun shouldConcatStrings() {
        assertThat(concat(listOf("hi"))).isEqualTo("hi")
        assertThat(concat(listOf("hi", "hello"))).isEqualTo("hi;hello")
        assertThat(concat(null)).isNull()
    }

    @Test
    fun shouldConvertNullableToString() {
        assertThat(nullableToString(null)).isNull()
        assertThat(nullableToString("test")).isEqualTo("test")
    }
}