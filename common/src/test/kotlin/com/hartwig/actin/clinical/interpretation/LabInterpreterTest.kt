package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.clinical.interpretation.LabInterpreter.interpret
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
        val firstKey = LabInterpreter.MAPPINGS.keys.first()
        val firstValue = LabInterpreter.MAPPINGS[firstKey]
        val values = listOf(
            LabInterpretationTestFactory.createMinimal().copy(code = firstKey.code, unit = firstKey.defaultUnit),
            LabInterpretationTestFactory.createMinimal().copy(code = firstValue!!.code, unit = firstValue.defaultUnit)
        )
        val interpretation = interpret(values)

        assertThat(interpretation.allValues(firstKey)!!).hasSize(1)
        assertThat(interpretation.allValues(firstValue)!!).hasSize(2)
        for (labValue in interpretation.allValues(firstValue)!!) {
            assertThat(labValue.code).isEqualTo(firstValue.code)
            assertThat(labValue.unit).isEqualTo(firstValue.defaultUnit)
        }
    }
}