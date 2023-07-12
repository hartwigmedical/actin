package com.hartwig.actin.database.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

public class DataUtilTest {

    @Test
    public void shouldConvertToByte() {
        assertThat(DataUtil.toByte(true)).isEqualTo((byte) 1);
        assertThat(DataUtil.toByte(false)).isEqualTo((byte) 0);
        assertThat(DataUtil.toByte(null)).isNull();
    }

    @Test
    public void shouldConcatStrings() {
        assertThat(DataUtil.concat(List.of("hi"))).isEqualTo("hi");
        assertThat(DataUtil.concat(List.of("hi", "hello"))).isEqualTo("hi;hello");
        assertThat(DataUtil.concat(null)).isNull();
    }

    @Test
    public void shouldConvertStreamToString() {
        assertThat(DataUtil.concatStream(Stream.of("first", "second"))).isEqualTo("first;second");
    }

    @Test
    public void shouldConvertNullableToString() {
        assertThat(DataUtil.nullableToString(null)).isNull();
        assertThat(DataUtil.nullableToString("test")).isEqualTo("test");
    }
}