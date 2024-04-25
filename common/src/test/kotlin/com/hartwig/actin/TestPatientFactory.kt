package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory

object TestPatientFactory {

    const val TEST_PATIENT = "ACTN01029999"
    const val TEST_SAMPLE = TEST_PATIENT + "T"

    fun createEmptyMolecularTestPatientRecord(): PatientRecord {
        return PatientRecordFactory.fromInputs(TestClinicalFactory.createProperTestClinicalRecord(), null)
    }

    fun createMinimalTestWGSPatientRecord(): PatientRecord {
        return PatientRecordFactory.fromInputs(
            TestClinicalFactory.createMinimalTestClinicalRecord(),
            TestMolecularFactory.createMinimalTestMolecularHistory()
        )
    }

    fun createProperTestPatientRecord(): PatientRecord {
        return PatientRecordFactory.fromInputs(
            TestClinicalFactory.createProperTestClinicalRecord(),
            TestMolecularFactory.createProperTestMolecularHistory()
        )
    }

    fun createExhaustiveTestPatientRecord(): PatientRecord {
        return PatientRecordFactory.fromInputs(
            TestClinicalFactory.createExhaustiveTestClinicalRecord(),
            TestMolecularFactory.createExhaustiveTestMolecularHistory()
        )
    }
}
