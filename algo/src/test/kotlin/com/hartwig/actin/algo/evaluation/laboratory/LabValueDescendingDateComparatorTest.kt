package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class LabValueDescendingDateComparatorTest {

    private val minimal = LabInterpretationTestFactory.createMinimal()
    private val value1 = minimal.copy(date = LocalDate.of(2018, 1, 1), measurement = LabMeasurement.HEMOGLOBIN, value = 0.0)
    private val value2 = minimal.copy(date = LocalDate.of(2019, 1, 1), measurement = LabMeasurement.CALCIUM, value = 0.0)
    private val value3 = minimal.copy(date = LocalDate.of(2018, 1, 1), measurement = LabMeasurement.CALCIUM, value = 1.0)
    private val value4 = minimal.copy(date = LocalDate.of(2018, 1, 1), measurement = LabMeasurement.CALCIUM, value = 2.0)
    private val values = listOf(value1, value2, value3, value4)

    @Test
    fun `Should sort lab values on date putting the highest value first if the same measure is measured multiple times on the same day`() {
        val sortedValues = values.sortedWith(LabValueDescendingDateComparator(true))
        assertThat(sortedValues).containsExactly(value2, value4, value3, value1)
    }

    @Test
    fun `Should sort lab values on date putting the lowest value first if the same measure is measured multiple times on the same day`() {
        val sortedValues = values.sortedWith(LabValueDescendingDateComparator(false))
        assertThat(sortedValues).containsExactly(value2, value3, value4, value1)
    }
}