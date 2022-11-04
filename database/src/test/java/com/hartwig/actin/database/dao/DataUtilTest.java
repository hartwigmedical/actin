package com.hartwig.actin.database.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Sets;

import org.junit.Test;

public class DataUtilTest {

    @Test
    public void canConvertToByte() {
        assertEquals(Byte.valueOf((byte) 1), DataUtil.toByte(true));
        assertEquals(Byte.valueOf((byte) 0), DataUtil.toByte(false));

        assertNull(DataUtil.toByte(null));
    }

    @Test
    public void canConcatStrings() {
        assertEquals("hi", DataUtil.concat(Sets.newHashSet("hi")));
        assertEquals("hi;hello", DataUtil.concat(Sets.newHashSet("hi", "hello")));

        assertNull(DataUtil.concat(null));
    }

    @Test
    public void canConvertNullableToString() {
        assertNull(DataUtil.nullableToString(null));
        assertEquals("test", DataUtil.nullableToString("test"));
    }
}