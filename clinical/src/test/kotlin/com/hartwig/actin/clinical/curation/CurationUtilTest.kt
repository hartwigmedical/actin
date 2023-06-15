package com.hartwig.actin.clinical.curation

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.curation.CurationUtil.capitalizeFirstLetterOnly
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import com.hartwig.actin.clinical.curation.CurationUtil.toCategories
import com.hartwig.actin.clinical.curation.CurationUtil.toDOIDs
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class CurationUtilTest {
    @Test
    fun canCapitalizeFirstLetterOnly() {
        Assert.assertEquals("Hi", capitalizeFirstLetterOnly("hi"))
        Assert.assertEquals("Hi", capitalizeFirstLetterOnly("Hi"))
        Assert.assertEquals("Hi", capitalizeFirstLetterOnly("hI"))
        Assert.assertEquals("H", capitalizeFirstLetterOnly("h"))
        Assert.assertEquals("H", capitalizeFirstLetterOnly("H"))
        Assert.assertEquals(Strings.EMPTY, capitalizeFirstLetterOnly(Strings.EMPTY))
    }

    @Test
    fun canFullTrim() {
        Assert.assertEquals(Strings.EMPTY, fullTrim(Strings.EMPTY))
        Assert.assertEquals("hi", fullTrim("hi"))
        Assert.assertEquals("this is a normal sentence", fullTrim("this is a normal sentence"))
        Assert.assertEquals("this is a weird sentence", fullTrim(" this     is  a weird   sentence  "))
    }

    @Test
    fun canConvertToDOIDs() {
        Assert.assertEquals(Sets.newHashSet("123"), toDOIDs("123"))
        val multiple = toDOIDs("123;456")
        Assert.assertEquals(2, multiple.size.toLong())
        Assert.assertTrue(multiple.contains("123"))
        Assert.assertTrue(multiple.contains("456"))
        Assert.assertTrue(toDOIDs(Strings.EMPTY).isEmpty())
    }

    @Test
    fun canConvertToCategories() {
        Assert.assertEquals(Sets.newHashSet("category1"), toCategories("category1"))
        val multiple = toCategories("category1;category2")
        Assert.assertEquals(2, multiple.size.toLong())
        Assert.assertTrue(multiple.contains("category1"))
        Assert.assertTrue(multiple.contains("category2"))
        Assert.assertTrue(toCategories(Strings.EMPTY).isEmpty())
    }
}