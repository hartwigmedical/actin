package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.ReportConfiguration

object TestReportFactory {
    fun createMinimalTestReport(): Report {
        val minimal = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return Report(
            patientId = TestPatientFactory.TEST_PATIENT,
            patientRecord = minimal.copy(tumor = minimal.tumor.copy(rawPathologyReport = "raw")),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
            config = ReportConfiguration(includeRawPathologyReport = true, includeMolecularEvidenceChapter = true)
        )
    }

    fun createProperTestReport(): Report {
        val proper = TestPatientFactory.createProperTestPatientRecord()
        return createMinimalTestReport().copy(
            patientRecord = proper.copy(tumor = proper.tumor.copy(rawPathologyReport = "raw")),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReport(): Report {
        val patientRecord = TestPatientFactory.createExhaustiveTestPatientRecord()
        return createMinimalTestReport().copy(
            patientRecord = patientRecord.copy(tumor = patientRecord.tumor.copy(rawPathologyReport = "raw")),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveTestReportWithoutMolecular(): Report {
        return createMinimalTestReport().copy(
            patientRecord = PatientRecordFactory.fromInputs(TestClinicalFactory.createExhaustiveTestClinicalRecord(), null),
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        )
    }

    fun createExhaustiveCRCTestReport(): Report {
        return createExhaustiveTestReport().copy(config = EnvironmentConfiguration.create(null, "CRC").report)
    }
}