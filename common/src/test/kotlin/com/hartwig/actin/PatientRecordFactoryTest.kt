package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PatientRecordFactoryTest {

    @Test
    fun `Should create patient record without molecular tests`() {
        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                emptyList()
            )
        ).isNotNull()
    }

    @Test
    fun `Should create patient record from test records`() {
        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalMolecularTests()
            )
        ).isNotNull()

        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperMolecularTests()
            )
        ).isNotNull()
    }
}