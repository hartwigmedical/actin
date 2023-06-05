package com.hartwig.actin.algo.evaluation.util

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class FormatTest {
    @Test
    fun canConcatStrings() {
        assertTrue(Format.concat(emptySet()).isEmpty())
        assertEquals("string", Format.concat(setOf("string")))
        assertEquals("string1; string2", Format.concat(setOf("string1", "string2")))
        assertEquals("string1", Format.concat(listOf("string1", "string1")))
    }

    @Test
    fun shouldFormatCaseInsensitive() {
        assertEquals("x and y", Format.concatLowercaseWithAnd(setOf("X", "Y")))
        assertEquals("x and y", Format.concatLowercaseWithAnd(setOf("x", "y")))
        assertEquals("x", Format.concatLowercaseWithAnd(setOf("X")))
    }

    @Test
    fun shouldSortIterablesBeforeConcat() {
        assertEquals("string1; string2; string3", Format.concat(listOf("string2", "string3", "string1")))
    }

    @Test
    fun canFormatDates() {
        assertNotNull(Format.date(LocalDate.of(2021, 8, 20)))
    }

    @Test
    fun canFormatPercentages() {
        assertEquals("50%", Format.percentage(0.500002))
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnIllegalPercentage() {
        Format.percentage(50.0)
    }
}