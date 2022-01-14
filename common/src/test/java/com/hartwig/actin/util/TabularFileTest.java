package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class TabularFileTest {

    @Test
    public void canCreateFields() {
        String[] header = new String[] { "header0", "header1", "header2" };
        Map<String, Integer> fields = TabularFile.createFields(header);

        assertEquals(0, (int) fields.get("header0"));
        assertEquals(1, (int) fields.get("header1"));
        assertEquals(2, (int) fields.get("header2"));
    }
}