package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class ResourceFileTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canCreateFieldIndexMap() {
        String[] header = new String[] { "header0", "header1", "header2" };
        Map<String, Integer> fieldIndexMap = ResourceFile.createFields(header);

        assertEquals(0, (int) fieldIndexMap.get("header0"));
        assertEquals(1, (int) fieldIndexMap.get("header1"));
        assertEquals(2, (int) fieldIndexMap.get("header2"));
    }

    @Test
    public void canParseStrings() {
        assertNull(ResourceFile.optionalString(Strings.EMPTY));
        assertEquals("hi", ResourceFile.optionalString("hi"));
    }

    @Test
    public void canParseBooleans() {
        assertNull(ResourceFile.optionalBool("unknown"));
        assertNull(ResourceFile.optionalBool(Strings.EMPTY));
        assertTrue(ResourceFile.optionalBool("1"));
        assertFalse(ResourceFile.optionalBool("0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnInvalidBoolean() {
        ResourceFile.bool("True");
    }

    @Test
    public void canParseDates() {
        assertNull(ResourceFile.optionalDate(Strings.EMPTY));
        assertEquals(LocalDate.of(2019, 4, 20), ResourceFile.optionalDate("2019-04-20"));
    }

    @Test
    public void canParseIntegers() {
        assertNull(ResourceFile.optionalInteger(Strings.EMPTY));
        assertEquals(4, (int) ResourceFile.optionalInteger("4"));
    }

    @Test
    public void canParseDoubles() {
        assertNull(ResourceFile.optionalNumber(Strings.EMPTY));
        assertEquals(4.2, ResourceFile.optionalNumber("4.2"), EPSILON);
    }
}