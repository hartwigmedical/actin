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
    public void canFullTrim() {
        assertEquals(Strings.EMPTY, CurationUtil.fullTrim(Strings.EMPTY));
        assertEquals("hi", CurationUtil.fullTrim("hi"));
        assertEquals("this is a normal sentence", CurationUtil.fullTrim("this is a normal sentence"));
        assertEquals("this is a weird sentence", CurationUtil.fullTrim(" this     is  a weird   sentence  "));
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

    @Test
    public void canConvertToCategories() {
        assertEquals(Sets.newHashSet("category1"), CurationUtil.toCategories("category1"));

        Set<String> multiple = CurationUtil.toCategories("category1;category2");
        assertEquals(2, multiple.size());
        assertTrue(multiple.contains("category1"));
        assertTrue(multiple.contains("category2"));

        assertTrue(CurationUtil.toCategories(Strings.EMPTY).isEmpty());
    }
}