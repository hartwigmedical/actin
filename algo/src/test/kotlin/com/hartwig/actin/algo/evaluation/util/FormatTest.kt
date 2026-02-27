package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import java.time.LocalDate
import junit.framework.TestCase.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatTest {

    @Test
    fun `Should lowercase strings and join with and`() {
        assertThat(Format.concatLowercaseWithAnd(setOf("X", "Y"))).isEqualTo("x and y")
        assertThat(Format.concatLowercaseWithAnd(setOf("x", "y"))).isEqualTo("x and y")
        assertThat(Format.concatLowercaseWithAnd(setOf("X"))).isEqualTo("x")
    }

    @Test
    fun `Should lowercase strings and join with and unless numeric`() {
        assertThat(Format.concatLowercaseUnlessNumericWithAnd(setOf("X", "y"))).isEqualTo("x and y")
        assertThat(Format.concatLowercaseUnlessNumericWithAnd(setOf("X"))).isEqualTo("x")
        assertThat(Format.concatLowercaseUnlessNumericWithAnd(setOf("X1", "X"))).isEqualTo("x and X1")
    }

    @Test
    fun `Should lowercase strings and join with comma and or`() {
        assertThat(Format.concatLowercaseWithCommaAndOr(emptySet())).isEqualTo("")
        assertThat(Format.concatLowercaseWithCommaAndOr(setOf("X"))).isEqualTo("x")
        assertThat(Format.concatLowercaseWithCommaAndOr(setOf("X", "Y"))).isEqualTo("x or y")
        assertThat(Format.concatLowercaseWithCommaAndOr(setOf("X", "Y", "Z"))).isEqualTo("x, y or z")
        assertThat(Format.concatLowercaseWithCommaAndOr(setOf("X", "Y", "Z", "A"))).isEqualTo("a, x, y or z")
    }

    @Test
    fun `Should lowercase strings and join with comma and and`() {
        assertThat(Format.concatLowercaseWithCommaAndAnd(emptySet())).isEqualTo("")
        assertThat(Format.concatLowercaseWithCommaAndAnd(setOf("X"))).isEqualTo("x")
        assertThat(Format.concatLowercaseWithCommaAndAnd(setOf("X", "Y"))).isEqualTo("x and y")
        assertThat(Format.concatLowercaseWithCommaAndAnd(setOf("X", "Y", "Z"))).isEqualTo("x, y and z")
        assertThat(Format.concatLowercaseWithCommaAndAnd(setOf("X", "Y", "Z", "A"))).isEqualTo("a, x, y and z")
    }

    @Test
    fun `Should join strings with comma and and`() {
        assertThat(Format.concat(emptySet())).isEqualTo("")
        assertThat(Format.concat(setOf("X"))).isEqualTo("X")
        assertThat(Format.concat(setOf("X", "Y"))).isEqualTo("X and Y")
        assertThat(Format.concat(setOf("X", "Y", "z"))).isEqualTo("X, Y and z")
        assertThat(Format.concat(setOf("X", "Y", "z", "A"))).isEqualTo("A, X, Y and z")
    }

    @Test
    fun `Should join strings with comma and or`() {
        assertThat(Format.concatWithCommaAndOr(emptySet())).isEqualTo("")
        assertThat(Format.concatWithCommaAndOr(listOf("X"))).isEqualTo("X")
        assertThat(Format.concatWithCommaAndOr(setOf("X", "Y"))).isEqualTo("X or Y")
        assertThat(Format.concatWithCommaAndOr(setOf("X", "Y", "z"))).isEqualTo("X, Y or z")
        assertThat(Format.concatWithCommaAndOr(setOf("X", "Y", "z", "A"))).isEqualTo("A, X, Y or z")
    }

    @Test
    fun `Should concat displayable items with custom display separated by and`() {
        assertThat(Format.concatItemsWithAnd(listOf(DrugType.HPV16_VACCINE, DrugType.ANTI_PD_1))).isEqualTo("anti-PD-1 and HPV-16 vaccine")
    }

    @Test
    fun `Should concat displayable items with custom display separated by or`() {
        assertThat(Format.concatItemsWithOr(listOf(DrugType.HPV16_VACCINE, DrugType.ANTI_PD_1))).isEqualTo("anti-PD-1 or HPV-16 vaccine")
    }

    @Test
    fun `Should format dates`() {
        assertNotNull(Format.date(LocalDate.of(2021, 8, 20)))
    }

    @Test
    fun `Should format percentages by rounding and adding percentage`() {
        assertThat(Format.percentage(0.500002)).isEqualTo("50%")
    }

    @Test
    fun `Should format lab references with ULN, possible reference and unit`() {
        assertThat(Format.labReferenceWithLimit(2.0, "ULN", 4.0, LabUnit.MICROMOLES_PER_LITER)).isEqualTo("2.0*ULN (8.0 umol/L)")
        assertThat(Format.labReferenceWithLimit(2.0, "ULN", null, LabUnit.MILLIGRAMS_PER_DECILITER)).isEqualTo("2.0*ULN (2.0*NA mg/dL)")
    }

    @Test
    fun `Should format lab values with first letter capitalized measure display, formatted value and display unit`() {
        assertThat(
            Format.labValue(
                LabMeasurement.INDIRECT_BILIRUBIN,
                4.005,
                LabUnit.MICROMOLES_PER_LITER
            )
        ).isEqualTo("Indirect bilirubin 4.0 umol/L")
    }

    @Test
    fun `Should join strings with and without gene prefix`() {
        assertThat(Format.concatVariants(setOf("BRAF V600E", "BRAF V600K"), "BRAF")).isEqualTo("V600E and V600K")
        assertThat(Format.concatVariants(setOf("BRAF x", "BRAF y", "BRAF z"), "BRAF")).isEqualTo("x, y and z")
        assertThat(Format.concatVariants(setOf("BRAF x"), "BRAF")).isEqualTo("x")
    }

    @Test
    fun `Should join strings with and without fusion suffix`() {
        assertThat(Format.concatFusions(setOf("X::Y fusion", "Z::A fusion"))).isEqualTo("X::Y and Z::A")
        assertThat(
            Format.concatFusions(
                setOf(
                    "X(exon1)::Y(exon2) fusion",
                    "Z(exon1)::A(exon2) fusion"
                )
            )
        ).isEqualTo("X(exon1)::Y(exon2) and Z(exon1)::A(exon2)")
        assertThat(Format.concatFusions(setOf("A::B fusion", "C::D fusion", "E::F fusion"))).isEqualTo("A::B, C::D and E::F")
        assertThat(Format.concatFusions(setOf("X::Y fusion"))).isEqualTo("X::Y")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should crash with illegal fraction`() {
        Format.percentage(50.0)
    }
}