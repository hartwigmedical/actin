package com.hartwig.actin.database.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatabaseUtilTest {

    @Test
    public void canConvertToByte() {
        assertEquals((byte) 1, DatabaseUtil.toByte(true));
        assertEquals((byte) 0, DatabaseUtil.toByte(false));
    }
}