package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.interpretation.LabInterpreter.interpret
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory.createProperTestClinicalRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabInterpreterTest {

    @Test
    fun `Should generate lab interpretation for empty list`() {
        assertThat(interpret(emptyList())).isNotNull
        assertThat(interpret(createProperTestClinicalRecord().labValues)).isNotNull
    }

    @Test
    fun `Should map values according to mapping`() {
        val (fromMeasurement, toMeasurement) = LabInterpreter.MAPPINGS.entries.first()
        val values = listOf(
            LabInterpretationTestFactory.createMinimal().copy(code = fromMeasurement.code, unit = fromMeasurement.defaultUnit),
            LabInterpretationTestFactory.createMinimal().copy(code = toMeasurement.code, unit = toMeasurement.defaultUnit)
        )
        val interpretation = interpret(values)

        assertThat(interpretation.allValues(fromMeasurement)!!).hasSize(1)
        assertThat(interpretation.allValues(toMeasurement)!!).hasSize(2)
        for (labValue in interpretation.allValues(toMeasurement)!!) {
            assertThat(labValue.code).isEqualTo(toMeasurement.code)
            assertThat(labValue.unit).isEqualTo(toMeasurement.defaultUnit)
        }
    }
}