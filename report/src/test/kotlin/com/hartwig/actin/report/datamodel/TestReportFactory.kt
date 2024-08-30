package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory

object TestReportFactory {
    fun createMinimalTestReport(): Report {
        return Report(
            patientId = TestPatientFactory.TEST_PATIENT,
            patientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
            config = ReportConfiguration()
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
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch(),
            config = ReportConfiguration(includeMolecularEvidenceChapter = true)
        )
    }

    fun createExhaustiveTestReportWithoutMolecular(): Report {
        return createMinimalTestReport().copy(
            patientRecord = PatientRecordFactory.fromInputs(TestClinicalFactory.createExhaustiveTestClinicalRecord(), null),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch(),
            config = ReportConfiguration(includeMolecularEvidenceChapter = true)
        )
    }

    fun createExhaustiveCRCTestReport(): Report {
        return createExhaustiveTestReport().copy(config = EnvironmentConfiguration.create(null, "CRC").report)
    }
}