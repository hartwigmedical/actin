package com.hartwig.actin.clinical.feed

import com.google.common.collect.Maps
import com.hartwig.actin.clinical.datamodel.Gender
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.apache.logging.log4j.util.Strings
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

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

        assertEquals(Strings.EMPTY, line.string("stringEmpty"))
        assertEquals(Strings.EMPTY, line.string("stringNull"))
        assertEquals("string ", line.string("stringProper"))
        assertEquals("string", line.trimmed("stringProper"))
        assertTrue(line.hasColumn("stringProper"))
        assertFalse(line.hasColumn("nonexistent"))

        assertEquals(Gender.MALE, line.gender("gender"))
        assertEquals(LocalDate.of(2019, 1, 1), line.date("date"))
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0), line.vitalFunctionDate("date"))
        assertEquals(LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0), line.bodyWeightDate("date"))
        assertEquals(LocalDate.of(2019, 1, 1), line.optionalDate("date"))

        assertEquals(1.0, line.number("number"), EPSILON)
        assertEquals(1.0, line.optionalNumber("number")!!, EPSILON)
        assertEquals(2, line.integer("integer").toLong())
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}