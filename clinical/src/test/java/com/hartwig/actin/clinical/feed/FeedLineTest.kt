package com.hartwig.actin.clinical.feed

import com.google.common.collect.Maps
import com.hartwig.actin.clinical.datamodel.Gender
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class FeedLineTest {
    @Test
    fun canQueryFeedLine() {
        val fields: MutableMap<String, Int> = Maps.newHashMap()
        fields["stringProper"] = 0
        fields["stringEmpty"] = 1
        fields["stringNull"] = 2
        fields["gender"] = 3
        fields["date"] = 4
        fields["number"] = 5
        fields["integer"] = 6
        val parts = arrayOf("string ", "", FeedLine.NULL_STRING, "Male", "2019-01-01 00:00:00.000", "1", "2")
        val line = FeedLine(fields, parts)
        Assert.assertEquals(Strings.EMPTY, line.string("stringEmpty"))
        Assert.assertEquals(Strings.EMPTY, line.string("stringNull"))
        Assert.assertEquals("string ", line.string("stringProper"))
        Assert.assertEquals("string", line.trimmed("stringProper"))
        Assert.assertTrue(line.hasColumn("stringProper"))
        Assert.assertFalse(line.hasColumn("nonexistent"))
        Assert.assertEquals(Gender.MALE, line.gender("gender"))
        Assert.assertEquals(LocalDate.of(2019, 1, 1), line.date("date"))
        Assert.assertEquals(LocalDate.of(2019, 1, 1), line.optionalDate("date"))
        Assert.assertEquals(1.0, line.number("number"), EPSILON)
        Assert.assertEquals(1.0, line.optionalNumber("number")!!, EPSILON)
        Assert.assertEquals(2, line.integer("integer").toLong())
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}