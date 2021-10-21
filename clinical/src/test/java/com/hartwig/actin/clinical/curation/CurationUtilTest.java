package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class CurationUtilTest {

    @Test
    public void canCapitalizeFirstLetterOnly() {
        assertEquals("Hi", CurationUtil.capitalizeFirstLetterOnly("hi"));
        assertEquals("Hi", CurationUtil.capitalizeFirstLetterOnly("Hi"));
        assertEquals("Hi", CurationUtil.capitalizeFirstLetterOnly("hI"));
        assertEquals("H", CurationUtil.capitalizeFirstLetterOnly("h"));
        assertEquals("H", CurationUtil.capitalizeFirstLetterOnly("H"));

        assertEquals(Strings.EMPTY, CurationUtil.capitalizeFirstLetterOnly(Strings.EMPTY));
    }

    @Test
    public void canConvertToDOIDs() {
        assertEquals(Sets.newHashSet("123"), CurationUtil.toDOIDs("123"));

        Set<String> multiple = CurationUtil.toDOIDs("123;456");
        assertEquals(2, multiple.size());
        assertTrue(multiple.contains("123"));
        assertTrue(multiple.contains("456"));

        assertTrue(CurationUtil.toDOIDs(Strings.EMPTY).isEmpty());
    }
}