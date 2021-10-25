package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.junit.Test;

public class FeedLineTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canQueryFeedLine() {
        Map<String, Integer> fields = Maps.newHashMap();
        fields.put("string", 0);
        fields.put("gender", 1);
        fields.put("date", 2);
        fields.put("number", 3);
        fields.put("integer", 4);

        String[] parts = new String[] { "string", "Male", "2019-01-01", "1", "2" };
        FeedLine line = new FeedLine(fields, parts);

        assertEquals("string", line.string("string"));
        assertEquals(Gender.MALE, line.gender("gender"));
        assertEquals(LocalDate.of(2019, 1, 1), line.date("date"));
        assertEquals(LocalDate.of(2019, 1, 1), line.optionalDate("date"));
        assertEquals(1D, line.number("number"), EPSILON);
        assertEquals(1D, line.optionalNumber("number"), EPSILON);
        assertEquals(2, line.integer("integer"));
    }
}