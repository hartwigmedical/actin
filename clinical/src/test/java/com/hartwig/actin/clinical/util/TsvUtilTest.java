package com.hartwig.actin.clinical.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class TsvUtilTest {

    @Test
    public void canCreateFieldIndexMap() {
        String[] header = new String[] { "header0", "header1", "header2" };
        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(header);

        assertEquals(0, (int) fieldIndexMap.get("header0"));
        assertEquals(1, (int) fieldIndexMap.get("header1"));
        assertEquals(2, (int) fieldIndexMap.get("header2"));
    }
}