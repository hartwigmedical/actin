package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.hartwig.actin.datamodel.clinical.Gender;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class FeedParseFunctionsTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canParseGender() {
        assertEquals(Gender.MALE, FeedParseFunctions.parseGender("Male"));
        assertEquals(Gender.FEMALE, FeedParseFunctions.parseGender("Female"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnInvalidGender() {
        FeedParseFunctions.parseGender("no gender");
    }

    @Test
    public void canParseDates() {
        assertNull(FeedParseFunctions.parseOptionalDate(Strings.EMPTY));

        LocalDate correct = LocalDate.of(2020, 10, 23);
        assertEquals(correct, FeedParseFunctions.parseOptionalDate("23-10-2020 13:10"));
        assertEquals(correct, FeedParseFunctions.parseOptionalDate("2020-10-23 13:10:55.0000000"));
        assertEquals(correct, FeedParseFunctions.parseOptionalDate("2020-10-23 13:10:55"));
        assertEquals(correct, FeedParseFunctions.parseOptionalDate("2020-10-23"));
    }

    @Test(expected = DateTimeParseException.class)
    public void crashOnInvalidDate() {
        FeedParseFunctions.parseDate("2020-23-10");
    }

    @Test
    public void canParseDoubles() {
        assertNull(FeedParseFunctions.parseOptionalDouble(Strings.EMPTY));
        assertEquals(2.1, FeedParseFunctions.parseOptionalDouble("2.1"), EPSILON);
        assertEquals(2.1, FeedParseFunctions.parseOptionalDouble("2,1"), EPSILON);
    }
}