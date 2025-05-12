package com.hartwig.actin.report.datamodel

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import java.time.LocalDate

object TestReportFactory {

    fun createMinimalTestReport(): Report {
        return Report(
            patientId = TestPatientFactory.TEST_PATIENT,
            patientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord(),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
            config = ReportConfiguration(),
            reportDate = LocalDate.now(),
            requestingHospital = "NKI-AvL"
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

    fun createExhaustiveTestReportWithOtherLocations(): Report {
        val matches = TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches
        val trialMatch1 = matches[0]
        val trialMatch2 = matches[1]
        val trialMatch3 = trialMatch1.copy(
            identification = TrialIdentification(
                trialId = "LKO3",
                open = true,
                acronym = "TEST-3",
                title = "Example test trial 3",
                nctId = "NCT00000003",
                phase = TrialPhase.PHASE_1,
                source = TrialSource.LKO,
                sourceId = "LKO-3",
                locations = setOf("Radboud UMC", "UMC Groningen"),
                url = "https://hartwigmedicalfoundation.nl"
            )
        )
        val trialMatch4 = trialMatch1.copy(
            identification = TrialIdentification(
                trialId = "LKO4",
                open = false,
                acronym = "TEST-4",
                title = "Example test trial 4",
                nctId = "NCT00000003",
                phase = TrialPhase.PHASE_2,
                source = TrialSource.LKO,
                sourceId = "LKO-4",
                locations = setOf("LUMC"),
                url = "https://hartwigmedicalfoundation.nl"
            )
        )
        val trialMatch5 = trialMatch1.copy(
            identification = TrialIdentification(
                trialId = "LKO5",
                open = true,
                acronym = "TEST-5",
                title = "Example test trial 5",
                nctId = "NCT00000005",
                phase = TrialPhase.PHASE_1,
                source = TrialSource.LKO,
                sourceId = "LKO-5",
                locations = setOf("LUMC"),
                url = "https://hartwigmedicalfoundation.nl"
            ),
            isPotentiallyEligible = false,
            cohorts = trialMatch1.cohorts.map { it.copy(isPotentiallyEligible = false) }
        )

        return createExhaustiveTestReport().copy(
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
                .copy(trialMatches = listOf(trialMatch1, trialMatch2, trialMatch3, trialMatch4, trialMatch5))
        )
    }
}