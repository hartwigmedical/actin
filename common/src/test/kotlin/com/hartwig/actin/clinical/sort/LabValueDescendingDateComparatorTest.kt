package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.interpretation.LabInterpretationTestFactory
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class LabValueDescendingDateComparatorTest {

    @Test
    fun `Should sort lab values`() {
        val minimal = LabInterpretationTestFactory.createMinimal()
        val value1 = minimal.copy(date = LocalDate.of(2018, 1, 1), measurement = LabMeasurement.HEMOGLOBIN, value = 0.0)
        val value2 = minimal.copy(date = LocalDate.of(2019, 1, 1), measurement = LabMeasurement.CALCIUM, value = 0.0)
        val value3 = minimal.copy(date = LocalDate.of(2018, 1, 1), measurement = LabMeasurement.CALCIUM, value = 1.0)
        val value4 = minimal.copy(date = LocalDate.of(2018, 1, 1), measurement = LabMeasurement.CALCIUM, value = 2.0)
        val values = listOf(value1, value2, value3, value4).sortedWith(LabValueDescendingDateComparator())

        assertThat(values).containsExactly(value2, value4, value3, value1)
    }
}