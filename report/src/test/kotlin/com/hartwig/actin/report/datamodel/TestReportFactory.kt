package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

object TestReportFactory {
    fun createMinimalTestReport(): Report {
        return Report(
            patientId = TestPatientFactory.TEST_PATIENT,
            patientRecord = TestPatientFactory.createMinimalTestPatientRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
        )
    }

    fun createProperTestReport(): Report {
        return createMinimalTestReport().copy(
            patientRecord = TestPatientFactory.createProperTestPatientRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReport(): Report {
        return createMinimalTestReport().copy(
            patientRecord = TestPatientFactory.createExhaustiveTestPatientRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReportWithoutMolecular(): Report {
        return createMinimalTestReport().copy(
            patientRecord = PatientRecordFactory.fromInputs(TestClinicalFactory.createExhaustiveTestClinicalRecord(), null),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }
}