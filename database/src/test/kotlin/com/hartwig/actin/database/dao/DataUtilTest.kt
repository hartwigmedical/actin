package com.hartwig.actin.database.dao

import com.hartwig.actin.database.dao.DataUtil.concat
import com.hartwig.actin.database.dao.DataUtil.concatObjects
import com.hartwig.actin.database.dao.DataUtil.nullableToString
import com.hartwig.actin.database.dao.DataUtil.toByte
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DataUtilTest {

    @Test
    fun `Should convert boolean to byte`() {
        assertThat(toByte(true)).isEqualTo(1.toByte())
        assertThat(toByte(false)).isEqualTo(0.toByte())
        assertThat(toByte(null)).isNull()
    }

    @Test
    fun `Should concat and sort strings`() {
        assertThat(concat(listOf("hi"))).isEqualTo("hi")
        assertThat(concat(listOf("hi", "hello"))).isEqualTo("hello;hi")
        assertThat(concat(null)).isNull()
    }

    @Test
    fun `Should concat and sort objects`() {
        assertThat(concatObjects(listOf(TestObject("hi")))).isEqualTo("TestObject(value=hi)")
        assertThat(concatObjects(listOf(TestObject("hi"), TestObject("hello")))).isEqualTo("TestObject(value=hello);TestObject(value=hi)")
    }

    @Test
    fun `Should convert nullable to string`() {
        assertThat(nullableToString(null)).isNull()
        assertThat(nullableToString("test")).isEqualTo("test")
    }

    data class TestObject(val value: String)
}