package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TsvUtilTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canCreateFieldIndexMap() {
        String[] header = new String[] { "header0", "header1", "header2" };
        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(header);

        assertEquals(0, (int) fieldIndexMap.get("header0"));
        assertEquals(1, (int) fieldIndexMap.get("header1"));
        assertEquals(2, (int) fieldIndexMap.get("header2"));
    }

    @Test
    public void canParseStrings() {
        assertNull(TsvUtil.optionalString(Strings.EMPTY));
        assertEquals("hi", TsvUtil.optionalString("hi"));
    }

    @Test
    public void canParseBooleans() {
        assertNull(TsvUtil.optionalBool("unknown"));
        assertNull(TsvUtil.optionalBool(Strings.EMPTY));
        assertTrue(TsvUtil.optionalBool("1"));
        assertFalse(TsvUtil.optionalBool("0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnInvalidBoolean() {
        TsvUtil.bool("True");
    }

    @Test
    public void canParseDates() {
        assertNull(TsvUtil.optionalDate(Strings.EMPTY));
        assertEquals(LocalDate.of(2019, 4, 20), TsvUtil.optionalDate("2019-04-20"));
    }

    @Test
    public void canParseIntegers() {
        assertNull(TsvUtil.optionalInteger(Strings.EMPTY));
        assertEquals(4, (int) TsvUtil.optionalInteger("4"));
    }

    @Test
    public void canParseDoubles() {
        assertNull(TsvUtil.optionalNumber(Strings.EMPTY));
        assertEquals(4.2, TsvUtil.optionalNumber("4.2"), EPSILON);
    }
}