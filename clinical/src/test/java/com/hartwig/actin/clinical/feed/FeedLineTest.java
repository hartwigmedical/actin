package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class FeedLineTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canQueryFeedLine() {
        Map<String, Integer> fields = Maps.newHashMap();
        fields.put("stringProper", 0);
        fields.put("stringEmpty", 1);
        fields.put("stringNull", 2);
        fields.put("gender", 3);
        fields.put("date", 4);
        fields.put("number", 5);
        fields.put("integer", 6);

        String[] parts = new String[] { "string ", "", FeedLine.NULL_STRING, "Male", "2019-01-01", "1", "2" };
        FeedLine line = new FeedLine(fields, parts);

        assertEquals(Strings.EMPTY, line.string("stringEmpty"));
        assertEquals(Strings.EMPTY, line.string("stringNull"));

        assertEquals("string ", line.string("stringProper"));
        assertEquals("string", line.trimmed("stringProper"));

        assertEquals(Gender.MALE, line.gender("gender"));
        assertEquals(LocalDate.of(2019, 1, 1), line.date("date"));
        assertEquals(LocalDate.of(2019, 1, 1), line.optionalDate("date"));
        assertEquals(1D, line.number("number"), EPSILON);
        assertEquals(1D, line.optionalNumber("number"), EPSILON);
        assertEquals(2, line.integer("integer"));
    }
}