package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;

import org.junit.Test;

public class CurationUtilTest {

    @Test
    public void canConvertDOIDs() {
        assertEquals(Sets.newHashSet("123"), CurationUtil.toDOIDs("123"));

        Set<String> multiple = CurationUtil.toDOIDs("123;456");
        assertEquals(2, multiple.size());
        assertTrue(multiple.contains("123"));
        assertTrue(multiple.contains("456"));
    }
}