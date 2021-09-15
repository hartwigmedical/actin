package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.StringJoiner;

import org.junit.Test;

public class FeedUtilTest {

    @Test
    public void canCreateFieldIndexMap() {
        String delimiter = "\t";

        StringJoiner header = new StringJoiner(delimiter);
        header.add("header0");
        header.add("header1");
        header.add("header2");

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(header.toString(), delimiter);

        assertEquals(0, (int) fieldIndexMap.get("header0"));
        assertEquals(1, (int) fieldIndexMap.get("header1"));
        assertEquals(2, (int) fieldIndexMap.get("header2"));
    }

    @Test
    public void canRemoveQuotes() {
        assertArrayEquals(new String[]{"test"}, FeedUtil.removeQuotes(new String[]{"\"test\""}));
        assertArrayEquals(new String[]{"test"}, FeedUtil.removeQuotes(new String[]{"test"}));
    }
}