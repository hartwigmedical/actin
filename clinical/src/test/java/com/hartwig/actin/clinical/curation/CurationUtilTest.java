package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class CurationUtilTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canCapitalizeFirstLetterOnly() {
        assertEquals("Hi", CurationUtil.capitalizeFirstLetterOnly("hi"));
        assertEquals("Hi", CurationUtil.capitalizeFirstLetterOnly("Hi"));
        assertEquals("Hi", CurationUtil.capitalizeFirstLetterOnly("hI"));
        assertEquals("H", CurationUtil.capitalizeFirstLetterOnly("h"));
        assertEquals("H", CurationUtil.capitalizeFirstLetterOnly("H"));

        assertEquals(Strings.EMPTY, CurationUtil.capitalizeFirstLetterOnly(Strings.EMPTY));
    }

    @Test
    public void canParseStrings() {
        assertNull(CurationUtil.optionalString(Strings.EMPTY));
        assertEquals("hi", CurationUtil.optionalString("hi"));
    }

    @Test
    public void canParseDOIDs() {
        assertEquals(Sets.newHashSet("123"), CurationUtil.parseDOID("123"));

        Set<String> multiple = CurationUtil.parseDOID("123;456");
        assertEquals(2, multiple.size());
        assertTrue(multiple.contains("123"));
        assertTrue(multiple.contains("456"));

        assertTrue(CurationUtil.parseDOID(Strings.EMPTY).isEmpty());
    }

    @Test
    public void canParseBooleans() {
        assertNull(CurationUtil.parseOptionalBoolean("unknown"));
        assertNull(CurationUtil.parseOptionalBoolean(Strings.EMPTY));
        assertTrue(CurationUtil.parseOptionalBoolean("1"));
        assertFalse(CurationUtil.parseOptionalBoolean("0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnInvalidBoolean() {
        CurationUtil.parseBoolean("True");
    }

    @Test
    public void canParseDates() {
        assertNull(CurationUtil.parseOptionalDate(Strings.EMPTY));
        assertEquals(LocalDate.of(2019, 4, 20), CurationUtil.parseOptionalDate("2019-04-20"));
    }

    @Test
    public void canParseIntegers() {
        assertNull(CurationUtil.parseOptionalInteger(Strings.EMPTY));
        assertEquals(4, (int) CurationUtil.parseOptionalInteger("4"));
    }

    @Test
    public void canParseDoubles() {
        assertNull(CurationUtil.parseOptionalDouble(Strings.EMPTY));
        assertEquals(4.2, CurationUtil.parseOptionalDouble("4.2"), EPSILON);
    }
}