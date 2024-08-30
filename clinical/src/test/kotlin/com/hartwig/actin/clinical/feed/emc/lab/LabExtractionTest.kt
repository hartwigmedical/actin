package com.hartwig.actin.clinical.feed.emc.lab

import com.hartwig.actin.clinical.feed.emc.TestFeedFactory
import com.hartwig.actin.clinical.feed.emc.lab.LabExtraction.extract
import com.hartwig.actin.clinical.feed.emc.lab.LabExtraction.extractLimits
import com.hartwig.actin.clinical.feed.emc.lab.LabExtraction.findSeparatingHyphenIndex
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.time.LocalDate

private const val EPSILON = 1.0E-10

class LabExtractionTest {

    @Test
    fun canExtractLabValues() {
        val testEntries = TestFeedFactory.createTestLabEntries()
        val lab1 = extract(findByCodeCodeOriginal(testEntries, "LAB1"))
        assertThat(lab1.date).isEqualTo(LocalDate.of(2018, 5, 29))
        assertThat(lab1.comparator).isEqualTo("")
        assertThat(lab1.value).isEqualTo(30.0, Offset.offset(EPSILON))
        assertThat(lab1.unit).isEqualTo(LabUnit.UNITS_PER_LITER)
        assertThat(lab1.refLimitLow!!).isEqualTo(20.0, Offset.offset(EPSILON))
        assertThat(lab1.refLimitUp!!).isEqualTo(40.0, Offset.offset(EPSILON))
        assertThat(lab1.isOutsideRef!!).isFalse
        
        val lab2 = extract(findByCodeCodeOriginal(testEntries, "LAB2"))
        assertThat(lab2.date).isEqualTo(LocalDate.of(2018, 5, 29))
        assertThat(lab2.comparator).isEqualTo("")
        assertThat(lab2.value).isEqualTo(22.0, Offset.offset(EPSILON))
        assertThat(lab2.unit).isEqualTo(LabUnit.MILLIMOLES_PER_LITER)
        assertThat(lab2.refLimitLow!!).isEqualTo(30.0, Offset.offset(EPSILON))
        assertThat(lab2.refLimitUp).isNull()
        assertThat(lab2.isOutsideRef!!).isTrue
        
        val lab3 = extract(findByCodeCodeOriginal(testEntries, "LAB3"))
        assertThat(lab3.date).isEqualTo(LocalDate.of(2018, 5, 29))
        assertThat(lab3.comparator).isEqualTo(">")
        assertThat(lab3.value).isEqualTo(50.0, Offset.offset(EPSILON))
        assertThat(lab3.unit).isEqualTo(LabUnit.MILLILITERS_PER_MINUTE)
        assertThat(lab3.refLimitLow!!).isEqualTo(50.0, Offset.offset(EPSILON))
        assertThat(lab3.refLimitUp).isNull()
        assertThat(lab3.isOutsideRef!!).isFalse
        
        val lab4 = extract(findByCodeCodeOriginal(testEntries, "LAB4"))
        assertThat(lab4.refLimitLow).isNull()
        assertThat(lab4.refLimitUp).isNull()
        assertThat(lab4.isOutsideRef).isNull()
    }

    @Test
    fun canExtractLimits() {
        val bothPositive = extractLimits("12 - 14")
        assertThat(bothPositive.lower!!).isEqualTo(12.0, Offset.offset(EPSILON))
        assertThat(bothPositive.upper!!).isEqualTo(14.0, Offset.offset(EPSILON))
        val bothOneNegative = extractLimits("-3 - 3")
        assertThat(bothOneNegative.lower!!).isEqualTo(-3.0, Offset.offset(EPSILON))
        assertThat(bothOneNegative.upper!!).isEqualTo(3.0, Offset.offset(EPSILON))
        val bothTwoNegative = extractLimits("-6 - -3")
        assertThat(bothTwoNegative.lower!!).isEqualTo(-6.0, Offset.offset(EPSILON))
        assertThat(bothTwoNegative.upper!!).isEqualTo(-3.0, Offset.offset(EPSILON))
        val lowerOnlyPositive = extractLimits("> 50")
        assertThat(lowerOnlyPositive.lower!!).isEqualTo(50.0, Offset.offset(EPSILON))
        assertThat(lowerOnlyPositive.upper).isNull()
        val lowerOnlyNegative = extractLimits("> -6")
        assertThat(lowerOnlyNegative.lower!!).isEqualTo(-6.0, Offset.offset(EPSILON))
        assertThat(lowerOnlyNegative.upper).isNull()
        val upperOnly = extractLimits("<90")
        assertThat(upperOnly.lower).isNull()
        assertThat(upperOnly.upper!!).isEqualTo(90.0, Offset.offset(EPSILON))
        val failed = extractLimits("not a limit")
        assertThat(failed.lower).isNull()
        assertThat(failed.upper).isNull()
    }

    @Test
    fun canFindSeparatingHyphen() {
        assertThat(findSeparatingHyphenIndex("3 - 5")).isEqualTo(2)
        assertThat(findSeparatingHyphenIndex("3,1 - 5,1")).isEqualTo(4)
        assertThat(findSeparatingHyphenIndex("-3-5")).isEqualTo(2)
        assertThat(findSeparatingHyphenIndex("-3--5")).isEqualTo(2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashFindSeparatingHyphenOnHyphenStarting() {
        findSeparatingHyphenIndex("-Nope")
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashFindSeparatingHyphenOnInvalidReferenceRangeText() {
        findSeparatingHyphenIndex("not a reference-range-text")
    }

    private fun findByCodeCodeOriginal(entries: List<LabEntry>, code: String): LabEntry {
        return entries.find { it.codeCodeOriginal == code } ?: throw IllegalStateException("Could not find lab entry with code: $code")
    }
}