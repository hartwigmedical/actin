package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.hartwig.actin.datamodel.clinical.Gender;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class FeedUtilTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canParseGender() {
        assertEquals(Gender.MALE, FeedUtil.parseGender("Male"));
        assertEquals(Gender.FEMALE, FeedUtil.parseGender("Female"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnInvalidGender() {
        FeedUtil.parseGender("no gender");
    }

    @Test
    public void canParseDates() {
        assertNull(FeedUtil.parseOptionalDate(Strings.EMPTY));

        LocalDate correct = LocalDate.of(2020, 10, 23);
        assertEquals(correct, FeedUtil.parseOptionalDate("23-10-2020 13:10"));
        assertEquals(correct, FeedUtil.parseOptionalDate("2020-10-23 13:10:55.0000000"));
        assertEquals(correct, FeedUtil.parseOptionalDate("2020-10-23 13:10:55"));
        assertEquals(correct, FeedUtil.parseOptionalDate("2020-10-23"));
    }

    @Test(expected = DateTimeParseException.class)
    public void crashOnInvalidDate() {
        FeedUtil.parseDate("2020-23-10");
    }

    @Test
    public void canParseDoubles() {
        assertNull(FeedUtil.parseOptionalDouble(Strings.EMPTY));
        assertEquals(2.1, FeedUtil.parseOptionalDouble("2.1"), EPSILON);
        assertEquals(2.1, FeedUtil.parseOptionalDouble("2,1"), EPSILON);
    }
}