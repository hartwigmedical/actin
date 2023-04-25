package com.hartwig.actin.algo.evaluation.util

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class FormatTest {
    @Test
    fun canConcatStrings() {
        assertTrue(Format.concat(Sets.newHashSet()).isEmpty())
        assertEquals("string", Format.concat(Sets.newHashSet("string")))
        assertEquals("string1; string2", Format.concat(Sets.newHashSet("string1", "string2")))
        assertEquals("string1", Format.concat(Lists.newArrayList("string1", "string1")))
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