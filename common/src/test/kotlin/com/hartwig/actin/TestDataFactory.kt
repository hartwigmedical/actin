package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory

object TestDataFactory {
    const val TEST_PATIENT = "ACTN01029999"
    const val TEST_SAMPLE = TEST_PATIENT + "T"

    fun createMinimalTestPatientRecord(): PatientRecord {
        return PatientRecord(
            patientId = TEST_PATIENT,
            patient = TestClinicalFactory.createTestPatientDetails(),
            tumor = TumorDetails(),
            clinicalStatus = ClinicalStatus(),
            oncologicalHistory = emptyList(),
            priorSecondPrimaries = emptyList(),
            priorOtherConditions = emptyList(),
            priorMolecularTests = emptyList(),
            complications = emptyList(),
            labValues = emptyList(),
            toxicities = emptyList(),
            intolerances = emptyList(),
            surgeries = emptyList(),
            bodyWeights = emptyList(),
            vitalFunctions = emptyList(),
            bloodTransfusions = emptyList(),
            medications = emptyList(),
            molecular = TestMolecularFactory.createMinimalTestMolecularRecord(),
        )
    }

    fun createProperTestPatientRecord(): PatientRecord {
        return createMinimalTestPatientRecord().copy(
            patientId = TEST_PATIENT,
            patient = TestClinicalFactory.createTestPatientDetails(),
            tumor = TestClinicalFactory.createTestTumorDetails(),
            clinicalStatus = TestClinicalFactory.createTestClinicalStatus(),
            oncologicalHistory = TestClinicalFactory.createTreatmentHistory(),
            priorSecondPrimaries = TestClinicalFactory.createTestPriorSecondPrimaries(),
            priorOtherConditions = TestClinicalFactory.createTestPriorOtherConditions(),
            priorMolecularTests = TestClinicalFactory.createTestPriorMolecularTests(),
            complications = TestClinicalFactory.createTestComplications(),
            labValues = TestClinicalFactory.createTestLabValues(),
            toxicities = TestClinicalFactory.createTestToxicities(),
            intolerances = TestClinicalFactory.createTestIntolerances(),
            surgeries = TestClinicalFactory.createTestSurgeries(),
            bodyWeights = TestClinicalFactory.createTestBodyWeights(),
            vitalFunctions = TestClinicalFactory.createTestVitalFunctions(),
            bloodTransfusions = TestClinicalFactory.createTestBloodTransfusions(),
            medications = TestClinicalFactory.createTestMedications(),
            molecular = TestMolecularFactory.createProperTestMolecularRecord()
        )
    }
}
