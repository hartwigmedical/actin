package com.hartwig.actin.clinical.interpretation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpretation.Companion.fromMeasurements
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class LabInterpretationTest {
    @Test
    fun canDealWithMissingLabValues() {
        val empty = fromMeasurements(ArrayListMultimap.create())
        Assert.assertNull(empty.mostRecentRelevantDate())
        Assert.assertTrue(empty.allDates().isEmpty())
        Assert.assertNull(empty.mostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE))
        Assert.assertNull(empty.secondMostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE))
        Assert.assertNull(empty.allValues(LabMeasurement.ALANINE_AMINOTRANSFERASE))
        Assert.assertTrue(
            empty.valuesOnDate(LabMeasurement.ALANINE_AMINOTRANSFERASE, TEST_DATE)!!.isEmpty()
        )
    }

    @Test
    fun canInterpretLabValues() {
        val measurements: Multimap<LabMeasurement, LabValue> = ArrayListMultimap.create()
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(1)).build())
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(5)).build())
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(3)).build())
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(2)).build())
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(4)).build())
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(4)).build())
        measurements.put(LabMeasurement.THROMBOCYTES_ABS, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(2)).build())
        measurements.put(LabMeasurement.THROMBOCYTES_ABS, LabInterpretationTestFactory.builder().date(TEST_DATE.minusDays(3)).build())
        val interpretation = fromMeasurements(measurements)
        val mostRecent = interpretation.mostRecentRelevantDate()
        Assert.assertEquals(TEST_DATE.minusDays(1), mostRecent)
        val allDates = interpretation.allDates()
        Assert.assertEquals(5, allDates.size.toLong())
        Assert.assertEquals(mostRecent, allDates.iterator().next())
        Assert.assertEquals(6, interpretation.allValues(LabMeasurement.ALBUMIN)!!.size.toLong())
        Assert.assertEquals(
            TEST_DATE.minusDays(1), interpretation.mostRecentValue(LabMeasurement.ALBUMIN)!!
                .date()
        )
        Assert.assertEquals(
            TEST_DATE.minusDays(2), interpretation.secondMostRecentValue(LabMeasurement.ALBUMIN)!!
                .date()
        )
        Assert.assertEquals(1, interpretation.valuesOnDate(LabMeasurement.ALBUMIN, TEST_DATE.minusDays(3))!!.size.toLong())
        Assert.assertEquals(2, interpretation.valuesOnDate(LabMeasurement.ALBUMIN, TEST_DATE.minusDays(4))!!.size.toLong())
        Assert.assertEquals(0, interpretation.valuesOnDate(LabMeasurement.ALBUMIN, TEST_DATE.minusDays(6))!!.size.toLong())
        Assert.assertEquals(2, interpretation.allValues(LabMeasurement.THROMBOCYTES_ABS)!!.size.toLong())
        Assert.assertEquals(
            TEST_DATE.minusDays(2), interpretation.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS)!!
                .date()
        )
        Assert.assertEquals(
            TEST_DATE.minusDays(3), interpretation.secondMostRecentValue(LabMeasurement.THROMBOCYTES_ABS)!!
                .date()
        )
        Assert.assertNull(interpretation.mostRecentValue(LabMeasurement.LEUKOCYTES_ABS))
        Assert.assertNull(interpretation.secondMostRecentValue(LabMeasurement.LEUKOCYTES_ABS))
        Assert.assertNull(interpretation.allValues(LabMeasurement.LEUKOCYTES_ABS))
        Assert.assertTrue(interpretation.valuesOnDate(LabMeasurement.LEUKOCYTES_ABS, mostRecent!!)!!.isEmpty())
    }

    companion object {
        private val TEST_DATE = LocalDate.of(2020, 1, 1)
    }
}