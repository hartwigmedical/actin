package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hartwig.actin.clinical.datamodel.Sex;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class FeedUtilTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canParseSex() {
        assertEquals(Sex.MALE, FeedUtil.parseSex("Male"));
        assertEquals(Sex.FEMALE, FeedUtil.parseSex("Female"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnInvalidSex() {
        FeedUtil.parseSex("no sex");
    }

    @Test
    public void canParseDates() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        assertNull(FeedUtil.parseOptionalDate(Strings.EMPTY, format));
        assertEquals(LocalDate.of(2020, 10, 23), FeedUtil.parseOptionalDate("23-10-2020", format));
    }

    @Test
    public void canParseDoubles() {
        assertNull(FeedUtil.parseOptionalDouble(Strings.EMPTY));
        assertEquals(2.1, FeedUtil.parseOptionalDouble("2.1"), EPSILON);
        assertEquals(2.1, FeedUtil.parseOptionalDouble("2,1"), EPSILON);
    }
}