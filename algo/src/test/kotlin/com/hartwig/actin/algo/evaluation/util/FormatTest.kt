package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import java.time.LocalDate

class FormatTest {
    @Test
    fun `Should lowercase strings and join with and`() {
        assertEquals("x and y", Format.concatLowercaseWithAnd(setOf("X", "Y")))
        assertEquals("x and y", Format.concatLowercaseWithAnd(setOf("x", "y")))
        assertEquals("x", Format.concatLowercaseWithAnd(setOf("X")))
    }

    @Test
    fun `Should lowercase strings and join with and unless numeric`() {
        assertEquals("x and y", Format.concatLowercaseUnlessNumericWithAnd(setOf("X", "y")))
        assertEquals("x", Format.concatLowercaseUnlessNumericWithAnd(setOf("X")))
        assertEquals("x and X1", Format.concatLowercaseUnlessNumericWithAnd(setOf("X1", "X")))
    }

    @Test
    fun `Should lowercase strings and join with comma and or`() {
        assertEquals("", Format.concatLowercaseWithCommaAndOr(emptySet()))
        assertEquals("x", Format.concatLowercaseWithCommaAndOr(setOf("X")))
        assertEquals("x or y", Format.concatLowercaseWithCommaAndOr(setOf("X", "Y")))
        assertEquals("x, y or z", Format.concatLowercaseWithCommaAndOr(setOf("X", "Y", "Z")))
        assertEquals("a, x, y or z", Format.concatLowercaseWithCommaAndOr(setOf("X", "Y", "Z", "A")))
    }

    @Test
    fun `Should lowercase strings and join with comma and and`() {
        assertEquals("", Format.concatLowercaseWithCommaAndAnd(emptySet()))
        assertEquals("x", Format.concatLowercaseWithCommaAndAnd(setOf("X")))
        assertEquals("x and y", Format.concatLowercaseWithCommaAndAnd(setOf("X", "Y")))
        assertEquals("x, y and z", Format.concatLowercaseWithCommaAndAnd(setOf("X", "Y", "Z")))
        assertEquals("a, x, y and z", Format.concatLowercaseWithCommaAndAnd(setOf("X", "Y", "Z", "A")))
    }

    @Test
    fun `Should join strings with comma and and`() {
        assertEquals("", Format.concat(emptySet()))
        assertEquals("X", Format.concat(setOf("X")))
        assertEquals("X and Y", Format.concat(setOf("X", "Y")))
        assertEquals("X, Y and z", Format.concat(setOf("X", "Y", "z")))
        assertEquals("A, X, Y and z", Format.concat(setOf("X", "Y", "z", "A")))
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
    fun `Should concat displayable items with custom display separated by and`() {
        assertEquals("anti-PD-1 and HPV-16 vaccine", Format.concatItemsWithAnd(listOf(DrugType.HPV16_VACCINE, DrugType.ANTI_PD_1)))
    }

    @Test
    fun `Should concat displayable items with custom display separated by or`() {
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
        assertEquals("2.0*ULN (8.0)", Format.labReference(2.0, "ULN", 4.0))
        assertEquals("2.0*ULN (2.0*NA)", Format.labReference(2.0, "ULN", null))
    }

    @Test
    fun canFormatLabValues() {
        assertEquals("Indirect bilirubin 4.0 umol/L", Format.labValue(LabMeasurement.INDIRECT_BILIRUBIN, 4.0, LabUnit.MICROMOLES_PER_LITER))
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnIllegalPercentage() {
        Format.percentage(50.0)
    }
}