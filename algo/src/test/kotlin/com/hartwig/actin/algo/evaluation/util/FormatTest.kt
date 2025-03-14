package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test

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
    fun `Should format dates`() {
        assertNotNull(Format.date(LocalDate.of(2021, 8, 20)))
    }

    @Test
    fun `Should format percentages by rounding and adding percentage`() {
        assertEquals("50%", Format.percentage(0.500002))
    }

    @Test
    fun `Should format lab references with ULN, possible reference and unit`() {
        assertEquals("2.0*ULN (8.0 umol/L)", Format.labReferenceWithLimit(2.0, "ULN", 4.0, LabUnit.MICROMOLES_PER_LITER))
        assertEquals("2.0*ULN (2.0*NA mg/dL)", Format.labReferenceWithLimit(2.0, "ULN", null, LabUnit.MILLIGRAMS_PER_DECILITER))
    }

    @Test
    fun `Should format lab values with first letter capitalized measure display, formatted value and display unit`() {
        assertEquals("Indirect bilirubin 4.0 umol/L", Format.labValue(LabMeasurement.INDIRECT_BILIRUBIN, 4.005, LabUnit.MICROMOLES_PER_LITER))
    }

    @Test
    fun `Should join strings with and without gene prefix`() {
        assertEquals("V600E and V600K", Format.concatVariants(setOf("BRAF V600E", "BRAF V600K"), "BRAF"))
        assertEquals("x, y and z", Format.concatVariants(setOf("BRAF x", "BRAF y", "BRAF z"), "BRAF"))
        assertEquals("x", Format.concatVariants(setOf("BRAF x"), "BRAF"))
    }

    @Test
    fun `Should join strings with and without fusion suffix`() {
        assertEquals("X::Y and Z::A", Format.concatFusions(setOf("X::Y fusion", "Z::A fusion")))
        assertEquals("A::B, C::D and E::F", Format.concatFusions(setOf("A::B fusion", "C::D fusion", "E::F fusion")))
        assertEquals("X::Y", Format.concatFusions(setOf("X::Y fusion")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should crash with illegal fraction`() {
        Format.percentage(50.0)
    }
}