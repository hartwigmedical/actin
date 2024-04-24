package com.hartwig.actin.report.pdf

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
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
import org.apache.logging.log4j.LogManager

class ReportContentProvider(private val report: Report, private val enableExtendedMode: Boolean = false) {

    fun provideChapters(): List<ReportChapter> {
        val (includeEfficacyEvidenceDetailsChapter, includeTrialMatchingDetailsChapter) = when {
            !enableExtendedMode -> {
                Pair(false, false)
            }

            report.config.showSOCLiteratureEfficacyEvidence -> {
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
            MolecularDetailsChapter(report, include = report.config.includeMolecularDetailsChapter),
            EfficacyEvidenceChapter(report, include = report.config.showSOCLiteratureEfficacyEvidence),
            ClinicalDetailsChapter(report),
            EfficacyEvidenceDetailsChapter(report, include = includeEfficacyEvidenceDetailsChapter),
            TrialMatchingChapter(report, enableExtendedMode),
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
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)

        val clinicalHistoryGenerator = if (report.config.includeOverviewWithClinicalHistorySummary) {
            PatientClinicalHistoryWithOverviewGenerator(report, keyWidth, valueWidth)
        } else {
            PatientClinicalHistoryGenerator(report, false, keyWidth, valueWidth)
        }

        val (openCohortsWithSlotsGenerator, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = true)
        val (openCohortsWithoutSlotsGenerator, _) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = false)

        val molecular = report.patientRecord.molecularHistory.latestMolecularRecord()
        val (dutchTrialGenerator, nonDutchTrialGenerator) = externalTrials(molecular, evaluated, contentWidth)
        return listOfNotNull(
            clinicalHistoryGenerator,
            if (report.config.showMolecularSummary) {
                molecular?.let { MolecularSummaryGenerator(report.patientRecord, it, cohorts, keyWidth, valueWidth) }
            } else null,
            if (report.config.showEligibleSOCTreatmentSummary) {
                SOCEligibleApprovedTreatmentGenerator(report, contentWidth)
            } else null,
            if (report.config.showApprovedTreatmentsInSummary) {
                EligibleApprovedTreatmentGenerator(report.patientRecord, contentWidth)
            } else null,
            openCohortsWithSlotsGenerator,
            openCohortsWithoutSlotsGenerator,
            dutchTrialGenerator,
            nonDutchTrialGenerator
        )
    }

    private fun externalTrials(
        molecular: MolecularRecord?, evaluated: List<EvaluatedCohort>, contentWidth: Float
    ): Pair<TableGenerator?, TableGenerator?> {
        if (molecular == null) {
            return Pair(null, null)
        } else {
            val externalEligibleTrials = AggregatedEvidenceInterpreter.filterAndGroupExternalTrialsByNctIdAndEvents(
                AggregatedEvidenceFactory.create(molecular).externalEligibleTrialsPerEvent, report.treatmentMatch.trialMatches
            )
            val externalTrialSummarizer = ExternalTrialSummarizer(report.config.filterTrialsWithOverlappingMolecularTargetsInSummary)
            val externalTrialSummary = externalTrialSummarizer.summarize(externalEligibleTrials, evaluated)
            return Pair(
                if (externalTrialSummary.dutchTrials.isNotEmpty()) {
                    EligibleDutchExternalTrialsGenerator(
                        molecular.externalTrialSource,
                        externalTrialSummary.dutchTrials,
                        contentWidth,
                        externalTrialSummary.dutchTrialsFiltered
                    )
                } else null,
                if (externalTrialSummary.otherCountryTrials.isNotEmpty()) {
                    EligibleOtherCountriesExternalTrialsGenerator(
                        molecular.externalTrialSource,
                        externalTrialSummary.otherCountryTrials,
                        contentWidth,
                        externalTrialSummary.otherCountryTrialsFiltered
                    )
                } else null
            )
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ReportContentProvider::class.java)
    }
}