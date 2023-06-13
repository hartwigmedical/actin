package com.hartwig.actin.clinical.feed.lab

import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.feed.TestFeedFactory
import com.hartwig.actin.clinical.feed.lab.LabExtraction.extract
import com.hartwig.actin.clinical.feed.lab.LabExtraction.extractLimits
import com.hartwig.actin.clinical.feed.lab.LabExtraction.findSeparatingHyphenIndex
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class LabExtractionTest {
    @Test
    fun canExtractLabValues() {
        val testEntries = TestFeedFactory.createTestLabEntries()
        val lab1 = extract(findByCodeCodeOriginal(testEntries, "LAB1"))
        Assert.assertEquals(LocalDate.of(2018, 5, 29), lab1.date())
        Assert.assertEquals(Strings.EMPTY, lab1.comparator())
        Assert.assertEquals(30.0, lab1.value(), EPSILON)
        Assert.assertEquals(LabUnit.UNITS_PER_LITER, lab1.unit())
        Assert.assertEquals(20.0, lab1.refLimitLow()!!, EPSILON)
        Assert.assertEquals(40.0, lab1.refLimitUp()!!, EPSILON)
        Assert.assertFalse(lab1.isOutsideRef!!)
        val lab2 = extract(findByCodeCodeOriginal(testEntries, "LAB2"))
        Assert.assertEquals(LocalDate.of(2018, 5, 29), lab2.date())
        Assert.assertEquals(Strings.EMPTY, lab2.comparator())
        Assert.assertEquals(22.0, lab2.value(), EPSILON)
        Assert.assertEquals(LabUnit.MILLIMOLES_PER_LITER, lab2.unit())
        Assert.assertEquals(30.0, lab2.refLimitLow()!!, EPSILON)
        Assert.assertNull(lab2.refLimitUp())
        Assert.assertTrue(lab2.isOutsideRef!!)
        val lab3 = extract(findByCodeCodeOriginal(testEntries, "LAB3"))
        Assert.assertEquals(LocalDate.of(2018, 5, 29), lab3.date())
        Assert.assertEquals(">", lab3.comparator())
        Assert.assertEquals(50.0, lab3.value(), EPSILON)
        Assert.assertEquals(LabUnit.MILLILITERS_PER_MINUTE, lab3.unit())
        Assert.assertEquals(50.0, lab3.refLimitLow()!!, EPSILON)
        Assert.assertNull(lab3.refLimitUp())
        Assert.assertFalse(lab3.isOutsideRef!!)
        val lab4 = extract(findByCodeCodeOriginal(testEntries, "LAB4"))
        Assert.assertNull(lab4.refLimitLow())
        Assert.assertNull(lab4.refLimitUp())
        Assert.assertNull(lab4.isOutsideRef)
    }

    @Test
    fun canExtractLimits() {
        val bothPositive = extractLimits("12 - 14")
        Assert.assertEquals(12.0, bothPositive.lower()!!, EPSILON)
        Assert.assertEquals(14.0, bothPositive.upper()!!, EPSILON)
        val bothOneNegative = extractLimits("-3 - 3")
        Assert.assertEquals(-3.0, bothOneNegative.lower()!!, EPSILON)
        Assert.assertEquals(3.0, bothOneNegative.upper()!!, EPSILON)
        val bothTwoNegative = extractLimits("-6 - -3")
        Assert.assertEquals(-6.0, bothTwoNegative.lower()!!, EPSILON)
        Assert.assertEquals(-3.0, bothTwoNegative.upper()!!, EPSILON)
        val lowerOnlyPositive = extractLimits("> 50")
        Assert.assertEquals(50.0, lowerOnlyPositive.lower()!!, EPSILON)
        Assert.assertNull(lowerOnlyPositive.upper())
        val lowerOnlyNegative = extractLimits("> -6")
        Assert.assertEquals(-6.0, lowerOnlyNegative.lower()!!, EPSILON)
        Assert.assertNull(lowerOnlyNegative.upper())
        val upperOnly = extractLimits("<90")
        Assert.assertNull(upperOnly.lower())
        Assert.assertEquals(90.0, upperOnly.upper()!!, EPSILON)
        val failed = extractLimits("not a limit")
        Assert.assertNull(failed.lower())
        Assert.assertNull(failed.upper())
    }

    @Test
    fun canFindSeparatingHyphen() {
        Assert.assertEquals(2, findSeparatingHyphenIndex("3 - 5").toLong())
        Assert.assertEquals(4, findSeparatingHyphenIndex("3,1 - 5,1").toLong())
        Assert.assertEquals(2, findSeparatingHyphenIndex("-3-5").toLong())
        Assert.assertEquals(2, findSeparatingHyphenIndex("-3--5").toLong())
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashFindSeparatingHyphenOnHyphenStarting() {
        findSeparatingHyphenIndex("-Nope")
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashFindSeparatingHyphenOnInvalidReferenceRangeText() {
        findSeparatingHyphenIndex("not a reference-range-text")
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private fun findByCodeCodeOriginal(entries: List<LabEntry>, code: String): LabEntry {
            for (entry in entries) {
                if (entry.codeCodeOriginal() == code) {
                    return entry
                }
            }
            throw IllegalStateException("Could not find lab entry with code: $code")
        }
    }
}