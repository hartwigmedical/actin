package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.laboratory.LabInterpretation.Companion.interpret
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class LabInterpretationTest {

    private val testDate = LocalDate.of(2020, 1, 1)

    @Test
    fun `Should return null or empty for queries on missing data`() {
        val empty = interpret(emptyList())
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
        val measurements =
            listOf(1, 5, 3, 2, 4, 4).map {
                minimalLabValue.copy(
                    date = testDate.minusDays(it.toLong()),
                    measurement = LabMeasurement.ALBUMIN
                )
            } +
                    listOf(2, 3).map {
                        minimalLabValue.copy(
                            date = testDate.minusDays(it.toLong()),
                            measurement = LabMeasurement.THROMBOCYTES_ABS
                        )
                    }
        val interpretation = interpret(measurements)
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