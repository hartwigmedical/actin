package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeedFileReaderTest {

    @Test
    public void canCleanQuotes() {
        String[] input = new String[] { "\"test\"", "test", "\"test \"\" test\"", "test \" test" };

        String[] cleaned = FeedFileReader.cleanQuotes(input);

        assertEquals("test", cleaned[0]);
        assertEquals("test", cleaned[1]);
        assertEquals("test \" test", cleaned[2]);
        assertEquals("test \" test", cleaned[3]);
    }

    @Test
    public void canFixLineBreaks() {
        String input = "test\\n\\ntest2\\ntest3";
        assertEquals("test\ntest2\ntest3", FeedFileReader.fixLineBreaks(input));
    }
}