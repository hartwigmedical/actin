package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory

object TestDataFactory {

    const val TEST_PATIENT = "ACTN01029999"
    const val TEST_SAMPLE = TEST_PATIENT + "T"

    fun createMinimalTestPatientRecord(): PatientRecord {
        return PatientRecord(
            patientId = TEST_PATIENT,
            clinical = TestClinicalFactory.createMinimalTestClinicalRecord(),
            molecular = TestMolecularFactory.createMinimalTestMolecularRecord(),
        )
    }

    fun createProperTestPatientRecord(): PatientRecord {
        return createMinimalTestPatientRecord().copy(
            clinical = TestClinicalFactory.createProperTestClinicalRecord(),
            molecular = TestMolecularFactory.createProperTestMolecularRecord()
        )
    }
}
