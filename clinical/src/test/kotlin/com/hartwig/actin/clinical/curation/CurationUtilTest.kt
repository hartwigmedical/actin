package com.hartwig.actin.clinical.curation

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.curation.CurationUtil.capitalizeFirstLetterOnly
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class CurationUtilTest {
    @Test
    fun shouldCapitalizeFirstLetterOnly() {
        Assert.assertEquals("Hi", capitalizeFirstLetterOnly("hi"))
        Assert.assertEquals("Hi", capitalizeFirstLetterOnly("Hi"))
        Assert.assertEquals("Hi", capitalizeFirstLetterOnly("hI"))
        Assert.assertEquals("H", capitalizeFirstLetterOnly("h"))
        Assert.assertEquals("H", capitalizeFirstLetterOnly("H"))
        Assert.assertEquals(Strings.EMPTY, capitalizeFirstLetterOnly(Strings.EMPTY))
    }

    @Test
    fun shouldTrimExtraSpacesBetweenWords() {
        Assert.assertEquals(Strings.EMPTY, fullTrim(Strings.EMPTY))
        Assert.assertEquals("hi", fullTrim("hi"))
        Assert.assertEquals("this is a normal sentence", fullTrim("this is a normal sentence"))
        Assert.assertEquals("this is a weird sentence", fullTrim(" this     is  a weird   sentence  "))
    }

    @Test
    fun shouldConvertDOIDsToSet() {
        assertConversionToSet("123", "456", CurationUtil::toDOIDs)
    }

    @Test
    fun shouldConvertCategoriesToSet() {
        assertConversionToSet("category1", "category2", CurationUtil::toCategories)
    }

    @Test
    fun shouldConvertToSet() {
        assertConversionToSet("treatment1", "treatment2", CurationUtil::toSet)
    }

    private fun assertConversionToSet(element1: String, element2: String, convert: (String) -> Set<String>) {
        Assert.assertEquals(Sets.newHashSet(element1), convert(element1))
        val multiple = convert("$element1;$element2")
        Assert.assertEquals(2, multiple.size.toLong())
        Assert.assertTrue(multiple.contains(element1))
        Assert.assertTrue(multiple.contains(element2))
        Assert.assertTrue(convert(Strings.EMPTY).isEmpty())
    }
}