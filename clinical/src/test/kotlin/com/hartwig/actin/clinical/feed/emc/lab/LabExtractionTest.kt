package com.hartwig.actin.clinical.feed.emc.lab

import com.hartwig.actin.clinical.feed.emc.TestFeedFactory
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.time.LocalDate

private const val EPSILON = 1.0E-10

class LabExtractionTest {

//    @Test
//    fun `Should extract lab values`() {
//        val testEntries = TestFeedFactory.createTestLabEntries()
//        val lab1 = extract(findByCodeCodeOriginal(testEntries, "LAB1"))
//        assertThat(lab1.date).isEqualTo(LocalDate.of(2018, 5, 29))
//        assertThat(lab1.comparator).isEqualTo("")
//        assertThat(lab1.value).isEqualTo(30.0, Offset.offset(EPSILON))
//        assertThat(lab1.unit).isEqualTo(LabUnit.UNITS_PER_LITER)
//        assertThat(lab1.refLimitLow!!).isEqualTo(20.0, Offset.offset(EPSILON))
//        assertThat(lab1.refLimitUp!!).isEqualTo(40.0, Offset.offset(EPSILON))
//        assertThat(lab1.isOutsideRef!!).isFalse
//
//        val lab2 = extract(findByCodeCodeOriginal(testEntries, "LAB2"))
//        assertThat(lab2.date).isEqualTo(LocalDate.of(2018, 5, 29))
//        assertThat(lab2.comparator).isEqualTo("")
//        assertThat(lab2.value).isEqualTo(22.0, Offset.offset(EPSILON))
//        assertThat(lab2.unit).isEqualTo(LabUnit.MILLIMOLES_PER_LITER)
//        assertThat(lab2.refLimitLow!!).isEqualTo(30.0, Offset.offset(EPSILON))
//        assertThat(lab2.refLimitUp).isNull()
//        assertThat(lab2.isOutsideRef!!).isTrue
//
//        val lab3 = extract(findByCodeCodeOriginal(testEntries, "LAB3"))
//        assertThat(lab3.date).isEqualTo(LocalDate.of(2018, 5, 29))
//        assertThat(lab3.comparator).isEqualTo(">")
//        assertThat(lab3.value).isEqualTo(50.0, Offset.offset(EPSILON))
//        assertThat(lab3.unit).isEqualTo(LabUnit.MILLILITERS_PER_MINUTE)
//        assertThat(lab3.refLimitLow!!).isEqualTo(50.0, Offset.offset(EPSILON))
//        assertThat(lab3.refLimitUp).isNull()
//        assertThat(lab3.isOutsideRef!!).isFalse
//
//        val lab4 = extract(findByCodeCodeOriginal(testEntries, "LAB4"))
//        assertThat(lab4.refLimitLow).isNull()
//        assertThat(lab4.refLimitUp).isNull()
//        assertThat(lab4.isOutsideRef).isNull()
//    }
//
//    @Test
//    fun `Should extract limits from referenceRangeString`() {
//        assertLimits("12 - 14", 12.0, 14.0)
//        assertLimits("-3 - 3", -3.0, 3.0)
//        assertLimits("-6 - -3", -6.0, -3.0)
//        assertLimits("> 50", 50.0, null)
//        assertLimits("> -6", -6.0, null)
//        assertLimits("<90", null, 90.0)
//        assertLimits("not a limit", null, null)
//        assertLimits("3,1 - 5,1", 3.1, 5.1)
//        assertLimits("-3-5", -3.0, 5.0)
//        assertLimits("-3--5", -3.0, -5.0)
//    }
//
//    private fun assertLimits(referenceRangeText: String, lower: Double?, upper: Double?) {
//        val extracted = extract(labEntryWithRange(referenceRangeText))
//        listOf(
//            extracted.refLimitLow to lower,
//            extracted.refLimitUp to upper
//        ).forEach { (actual, expected) ->
//            if (expected == null) {
//                assertThat(actual).isNull()
//            } else {
//                assertThat(actual).isEqualTo(expected, Offset.offset(EPSILON))
//            }
//        }
//    }
//
//    @Test(expected = IllegalArgumentException::class)
//    fun `Should throw for lab entry with leading hyphen but no measurement`() {
//        extract(labEntryWithRange("-Nope"))
//    }
//
//    @Test(expected = IllegalArgumentException::class)
//    fun `Should throw for lab entry with internal hyphen but no measurement`() {
//        extract(labEntryWithRange("not a reference-range-text"))
//    }
//
//    private fun findByCodeCodeOriginal(entries: List<LabEntry>, code: String): LabEntry {
//        return entries.find { it.codeCodeOriginal == code } ?: throw IllegalStateException("Could not find lab entry with code: $code")
//    }
//
//    private fun labEntryWithRange(referenceRangeText: String): LabEntry {
//        return LabEntry(
//            subject = "test",
//            codeCodeOriginal = "test",
//            codeDisplayOriginal = "test",
//            valueQuantityComparator = "test",
//            valueQuantityValue = 0.0,
//            valueQuantityUnit = "mmol/L",
//            referenceRangeText = referenceRangeText,
//            effectiveDateTime = LocalDate.of(2024, 11, 22)
//        )
//    }
}