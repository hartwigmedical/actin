package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatientRecordFactoryTest {

    @Test
    fun `Should create patient record without molecular record`() {
        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                null
            )
        ).isNotNull
    }

    @Test
    fun `Should create patient record from test records`() {
        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularHistory()
            )
        ).isNotNull

        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularHistory()
            )
        ).isNotNull
    }
}