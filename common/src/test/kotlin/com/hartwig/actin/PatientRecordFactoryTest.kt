package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatientRecordFactoryTest {

    @Test
    fun `Should create patient record from test records`() {
        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularRecord()
            )
        ).isNotNull

        assertThat(
            PatientRecordFactory.fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularRecord()
            )
        ).isNotNull
    }

    @Test
    fun `Should use clinical patient ID over molecular patient ID when different`() {
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(patientId = "clinical")
        val molecular = TestMolecularFactory.createMinimalTestMolecularRecord().copy(patientId = "molecular")
        
        val patient = PatientRecordFactory.fromInputs(clinical, molecular)
        assertThat(patient.patientId).isEqualTo("clinical")
    }
}