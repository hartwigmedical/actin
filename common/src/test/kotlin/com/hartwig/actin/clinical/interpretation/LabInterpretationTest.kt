package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.interpretation.LabInterpretation.Companion.fromMeasurements
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class LabInterpretationTest {

    private val testDate = LocalDate.of(2020, 1, 1)
    
    @Test
    fun `Should return null or empty for queries on missing data`() {
        val empty = fromMeasurements(emptyMap())
        assertThat(empty.mostRecentRelevantDate()).isNull()
        assertThat(empty.allDates()).isEmpty()
        assertThat(empty.mostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE)).isNull()
        assertThat(empty.secondMostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE)).isNull()
        assertThat(empty.allValues(LabMeasurement.ALANINE_AMINOTRANSFERASE)).isNull()
        assertThat(empty.valuesOnDate(LabMeasurement.ALANINE_AMINOTRANSFERASE, testDate)).isEmpty()
    }

    @Test
    fun `Should interpret lab values`() {
        val minimalLabValue = LabInterpretationTestFactory.createMinimal()
        val measurements = mapOf(
            LabMeasurement.ALBUMIN to listOf(1, 5, 3, 2, 4, 4).map { minimalLabValue.copy(date = testDate.minusDays(it.toLong())) },
            LabMeasurement.THROMBOCYTES_ABS to listOf(2, 3).map { minimalLabValue.copy(date = testDate.minusDays(it.toLong())) }
        )
        val interpretation = fromMeasurements(measurements)
        val mostRecent = interpretation.mostRecentRelevantDate()
        assertThat(mostRecent).isEqualTo(testDate.minusDays(1))
        val allDates = interpretation.allDates()

        assertThat(allDates).hasSize(5)
        assertThat(allDates.iterator().next()).isEqualTo(mostRecent)
        assertThat(interpretation.allValues(LabMeasurement.ALBUMIN)!!).hasSize(6)
        assertThat(interpretation.mostRecentValue(LabMeasurement.ALBUMIN)!!.date).isEqualTo(testDate.minusDays(1))
        assertThat(interpretation.secondMostRecentValue(LabMeasurement.ALBUMIN)!!.date).isEqualTo(testDate.minusDays(2))
        assertThat(interpretation.valuesOnDate(LabMeasurement.ALBUMIN, testDate.minusDays(3))).hasSize(1)
        assertThat(interpretation.valuesOnDate(LabMeasurement.ALBUMIN, testDate.minusDays(4))).hasSize(2)
        assertThat(interpretation.valuesOnDate(LabMeasurement.ALBUMIN, testDate.minusDays(6))).hasSize(0)

        assertThat(interpretation.allValues(LabMeasurement.THROMBOCYTES_ABS)!!).hasSize(2)
        assertThat(interpretation.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS)!!.date).isEqualTo(testDate.minusDays(2))
        assertThat(interpretation.secondMostRecentValue(LabMeasurement.THROMBOCYTES_ABS)!!.date).isEqualTo(testDate.minusDays(3))

        assertThat(interpretation.mostRecentValue(LabMeasurement.LEUKOCYTES_ABS)).isNull()
        assertThat(interpretation.secondMostRecentValue(LabMeasurement.LEUKOCYTES_ABS)).isNull()
        assertThat(interpretation.allValues(LabMeasurement.LEUKOCYTES_ABS)).isNull()
        assertThat(interpretation.valuesOnDate(LabMeasurement.LEUKOCYTES_ABS, mostRecent!!)).isEmpty()
    }
}