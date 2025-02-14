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
}