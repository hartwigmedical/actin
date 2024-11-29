package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class FormatTest {
    @Test
    fun shouldConcatStrings() {
        assertTrue(Format.concat(emptySet()).isEmpty())
        assertEquals("string", Format.concat(setOf("string")))
        assertEquals("string1; string2", Format.concat(setOf("string1", "string2")))
        assertEquals("string1", Format.concat(listOf("string1", "string1")))
    }

    @Test
    fun shouldSortIterablesBeforeConcat() {
        assertEquals("string1; string2; string3", Format.concat(listOf("string2", "string3", "string1")))
    }

    @Test
    fun shouldConcatDisplayableItemsWithCustomDisplaySeparatedBySemicolon() {
        assertEquals("anti-PD-1; HPV-16 vaccine", Format.concatItems(listOf(DrugType.HPV16_VACCINE, DrugType.ANTI_PD_1)))
    }

    @Test
    fun shouldLowercaseStringsAndJoinWithAnd() {
        assertEquals("x and y", Format.concatLowercaseWithAnd(setOf("X", "Y")))
        assertEquals("x and y", Format.concatLowercaseWithAnd(setOf("x", "y")))
        assertEquals("x", Format.concatLowercaseWithAnd(setOf("X")))
    }

    @Test
    fun shouldLowercaseStringsAndJoinWithCommaAndOr() {
        assertEquals("", Format.concatLowercaseWithCommaAndOr(emptySet()))
        assertEquals("x", Format.concatLowercaseWithCommaAndOr(setOf("X")))
        assertEquals("x or y", Format.concatLowercaseWithCommaAndOr(setOf("X", "Y")))
        assertEquals("x, y or z", Format.concatLowercaseWithCommaAndOr(setOf("X", "Y", "Z")))
        assertEquals("a, x, y or z", Format.concatLowercaseWithCommaAndOr(setOf("X", "Y", "Z", "A")))
    }

    @Test
    fun `Should join strings with comma and and`() {
        assertEquals("", Format.concatWithCommaAndAnd(emptySet()))
        assertEquals("X", Format.concatWithCommaAndAnd(setOf("X")))
        assertEquals("X and Y", Format.concatWithCommaAndAnd(setOf("X", "Y")))
        assertEquals("X, Y and z", Format.concatWithCommaAndAnd(setOf("X", "Y", "z")))
        assertEquals("A, X, Y and z", Format.concatWithCommaAndAnd(setOf("X", "Y", "z", "A")))
    }

    @Test
    fun `Should join strings with comma and or`() {
        assertEquals("", Format.concatWithCommaAndOr(emptySet()))
        assertEquals("X", Format.concatWithCommaAndOr(listOf("X")))
        assertEquals("X or Y", Format.concatWithCommaAndOr(setOf("X", "Y")))
        assertEquals("X, Y or z", Format.concatWithCommaAndOr(setOf("X", "Y", "z")))
        assertEquals("A, X, Y or z", Format.concatWithCommaAndOr(setOf("X", "Y", "z", "A")))
    }

    @Test
    fun shouldConcatDisplayableItemsWithCustomDisplaySeparatedByAnd() {
        assertEquals("anti-PD-1 and HPV-16 vaccine", Format.concatItemsWithAnd(listOf(DrugType.HPV16_VACCINE, DrugType.ANTI_PD_1)))
    }

    @Test
    fun shouldConcatDisplayableItemsWithCustomDisplaySeparatedByOr() {
        assertEquals("anti-PD-1 or HPV-16 vaccine", Format.concatItemsWithOr(listOf(DrugType.HPV16_VACCINE, DrugType.ANTI_PD_1)))
    }

    @Test
    fun canFormatDates() {
        assertNotNull(Format.date(LocalDate.of(2021, 8, 20)))
    }

    @Test
    fun canFormatPercentages() {
        assertEquals("50%", Format.percentage(0.500002))
    }

    @Test
    fun canFormatLabReferences() {
        assertEquals("2.0*ULN (2.0*4.0)", Format.labReference(2.0, "ULN", 4.0))
    }

    @Test
    fun canFormatLabValues() {
        assertEquals("Indirect bilirubin 4.0", Format.labValue(LabMeasurement.INDIRECT_BILIRUBIN, 4.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnIllegalPercentage() {
        Format.percentage(50.0)
    }
}