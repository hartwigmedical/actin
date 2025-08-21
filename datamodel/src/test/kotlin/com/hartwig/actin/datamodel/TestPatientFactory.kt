package com.hartwig.actin.datamodel

import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory

object TestPatientFactory {

    const val TEST_PATIENT = "ACTN01029999"
    const val TEST_SAMPLE = TEST_PATIENT + "T"

    fun createEmptyMolecularTestPatientRecord(): PatientRecord {
        return create(TestClinicalFactory.createProperTestClinicalRecord(), null)
    }

    fun createMinimalTestWGSPatientRecord(): PatientRecord {
        return create(
            TestClinicalFactory.createMinimalTestClinicalRecord(),
            TestMolecularFactory.createMinimalMolecularTests()
        )
    }

    fun createProperTestPatientRecord(): PatientRecord {
        return create(
            TestClinicalFactory.createProperTestClinicalRecord(),
            TestMolecularFactory.createProperMolecularTests()
        )
    }

    fun createExhaustiveTestPatientRecord(): PatientRecord {
        return create(
            TestClinicalFactory.createExhaustiveTestClinicalRecord(),
            TestMolecularFactory.createExhaustiveMolecularTests()
        )
    }

    private fun create(clinical: ClinicalRecord, molecular: MolecularHistory?): PatientRecord {
        return PatientRecord(
            patientId = clinical.patientId,
            patient = clinical.patient,
            tumor = clinical.tumor,
            clinicalStatus = clinical.clinicalStatus,
            performanceStatus = clinical.performanceStatus,
            oncologicalHistory = clinical.oncologicalHistory,
            priorPrimaries = clinical.priorPrimaries,
            comorbidities = clinical.comorbidities,
            labValues = clinical.labValues,
            surgeries = clinical.surgeries,
            bodyWeights = clinical.bodyWeights,
            bodyHeights = clinical.bodyHeights,
            vitalFunctions = clinical.vitalFunctions,
            bloodTransfusions = clinical.bloodTransfusions,
            medications = clinical.medications,
            ihcTests = clinical.ihcTests,
            pathologyReports = clinical.pathologyReports,
            molecularTests = molecular ?: MolecularHistory.empty()
        )
    }
}
