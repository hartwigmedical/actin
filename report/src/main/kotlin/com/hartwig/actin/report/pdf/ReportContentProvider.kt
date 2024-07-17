package com.hartwig.actin.report.pdf

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.PersonalizedEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryWithOverviewGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCEligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleDutchExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleOtherCountriesExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
import org.apache.logging.log4j.LogManager

class ReportContentProvider(private val report: Report, private val enableExtendedMode: Boolean = false) {

    fun provideChapters(): List<ReportChapter> {
        val (includeEfficacyEvidenceDetailsChapter, includeTrialMatchingDetailsChapter) = when {
            !enableExtendedMode -> {
                Pair(false, false)
            }

            report.config.includeSOCLiteratureEfficacyEvidence -> {
                LOGGER.info("Including SOC literature details")
                Pair(true, false)
            }

            else -> {
                LOGGER.info("Including trial matching details")
                Pair(false, true)
            }
        }

        return listOf(
            SummaryChapter(report),
            PersonalizedEvidenceChapter(
                report, include = report.config.includeSOCLiteratureEfficacyEvidence && report.treatmentMatch.personalizedDataAnalysis != null
            ),
            MolecularDetailsChapter(report, include = report.config.includeMolecularDetailsChapter),
            EfficacyEvidenceChapter(report, include = report.config.includeSOCLiteratureEfficacyEvidence),
            ClinicalDetailsChapter(report, include = report.config.includeClinicalDetailsChapter),
            EfficacyEvidenceDetailsChapter(report, include = includeEfficacyEvidenceDetailsChapter),
            TrialMatchingChapter(report, enableExtendedMode, report.config.includeIneligibleTrialsInSummary),
            TrialMatchingDetailsChapter(report, include = includeTrialMatchingDetailsChapter)
        ).filter(ReportChapter::include)
    }

    fun provideClinicalDetailsTables(keyWidth: Float, valueWidth: Float, contentWidth: Float): List<TableGenerator> {
        val bloodTransfusions = report.patientRecord.bloodTransfusions

        return listOfNotNull(
            PatientClinicalHistoryGenerator(report, true, keyWidth, valueWidth),
            PatientCurrentDetailsGenerator(report.patientRecord, keyWidth, valueWidth),
            TumorDetailsGenerator(report.patientRecord, keyWidth, valueWidth),
            report.patientRecord.medications?.let {
                MedicationGenerator(it, contentWidth, MedicationStatusInterpreterOnEvaluationDate(report.treatmentMatch.referenceDate))
            },
            if (bloodTransfusions.isEmpty()) null else BloodTransfusionGenerator(bloodTransfusions, contentWidth)
        )
    }

    fun provideSummaryTables(keyWidth: Float, valueWidth: Float, contentWidth: Float): List<TableGenerator> {
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)

        val clinicalHistoryGenerator = if (report.config.includeOverviewWithClinicalHistorySummary) {
            PatientClinicalHistoryWithOverviewGenerator(report, cohorts, keyWidth, valueWidth)
        } else {
            PatientClinicalHistoryGenerator(report, false, keyWidth, valueWidth)
        }

        val (openCohortsWithSlotsGenerator, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = true)
        val (openCohortsWithoutSlotsGenerator, _) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = false)

        val (dutchTrialGenerator, nonDutchTrialGenerator) = externalTrials(report.patientRecord, evaluated, contentWidth)
        return listOfNotNull(
            clinicalHistoryGenerator,
            if (report.config.includeMolecularSummary && report.patientRecord.molecularHistory.molecularTests.isNotEmpty()) {
                MolecularSummaryGenerator(report.patientRecord, cohorts, keyWidth, valueWidth)
            } else null,
            if (report.config.includeEligibleSOCTreatmentSummary) {
                SOCEligibleApprovedTreatmentGenerator(report, contentWidth)
            } else null,
            if (report.config.includeApprovedTreatmentsInSummary) {
                EligibleApprovedTreatmentGenerator(report.patientRecord, contentWidth)
            } else null,
            if (report.config.includeTrialMatchingSummary) {
                openCohortsWithSlotsGenerator
            } else null,
            if (report.config.includeTrialMatchingSummary) {
                openCohortsWithoutSlotsGenerator
            } else null,
            dutchTrialGenerator,
            nonDutchTrialGenerator,
            if (report.config.includeIneligibleTrialsInSummary) {
                IneligibleActinTrialsGenerator.fromEvaluatedCohorts(
                    cohorts,
                    report.treatmentMatch.trialSource,
                    contentWidth,
                    enableExtendedMode
                )
            } else null
        )
    }

    private fun externalTrials(
        patientRecord: PatientRecord, evaluated: List<EvaluatedCohort>, contentWidth: Float
    ): Pair<TableGenerator?, TableGenerator?> {
        val externalEligibleTrials =
            patientRecord.molecularHistory.molecularTests.map { AggregatedEvidenceFactory.create(it).externalEligibleTrialsPerEvent }
                .flatMap { it.entries }
                .associate { it.toPair() }

        val externalTrialSummarizer = ExternalTrialSummarizer(report.config.countryOfResidence)
        val externalTrialSummary = externalTrialSummarizer.summarize(
            externalEligibleTrials,
            report.treatmentMatch.trialMatches,
            evaluated
        )
        return Pair(
            if (externalTrialSummary.localTrials.isNotEmpty()) {
                EligibleDutchExternalTrialsGenerator(
                    "CKB",
                    externalTrialSummary.localTrials,
                    contentWidth,
                    externalTrialSummary.localTrialsFiltered
                )
            } else null,
            if (externalTrialSummary.nonLocalTrials.isNotEmpty()) {
                EligibleOtherCountriesExternalTrialsGenerator(
                    "CKB",
                    externalTrialSummary.nonLocalTrials,
                    contentWidth,
                    externalTrialSummary.nonLocalTrialsFiltered
                )
            } else null
        )
    }


    companion object {
        private val LOGGER = LogManager.getLogger(ReportContentProvider::class.java)
    }
}