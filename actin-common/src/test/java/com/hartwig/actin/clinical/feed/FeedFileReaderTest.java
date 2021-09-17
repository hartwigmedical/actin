package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.StringJoiner;

import org.junit.Test;

public class FeedFileReaderTest {

    @Test
    public void canCreateFieldIndexMap() {
        String delimiter = "\t";

        StringJoiner header = new StringJoiner(delimiter);
        header.add("header0");
        header.add("header1");
        header.add("header2");

        Map<String, Integer> fieldIndexMap = FeedFileReader.createFieldIndexMap(header.toString(), delimiter);

        assertEquals(0, (int) fieldIndexMap.get("header0"));
        assertEquals(1, (int) fieldIndexMap.get("header1"));
        assertEquals(2, (int) fieldIndexMap.get("header2"));
    }

    @Test
    public void canCleanQuotes() {
        String[] input = new String[] { "\"test\"", "test", "\"test \"\" test\"", "test \" test" };

        String[] cleaned = FeedFileReader.cleanQuotes(input);

        assertEquals("test", cleaned[0]);
        assertEquals("test", cleaned[1]);
        assertEquals("test \" test", cleaned[2]);
        assertEquals("test \" test", cleaned[3]);
    }

}