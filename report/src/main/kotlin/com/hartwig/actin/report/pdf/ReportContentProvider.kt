package com.hartwig.actin.report.pdf

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.configuration.MolecularSummaryType
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceDetailsChapter
import com.hartwig.actin.report.pdf.chapters.LongitudinalMolecularHistoryChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.PersonalizedEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.ResistanceEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryWithOverviewGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCEligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.trial.ExternalTrials
import com.hartwig.actin.report.trial.TrialsProvider
import com.hartwig.actin.treatment.EvidenceScoringModel
import com.hartwig.actin.treatment.TreatmentRankingModel
import com.hartwig.actin.treatment.createScoringConfig
import org.apache.logging.log4j.LogManager

class ReportContentProvider(private val report: Report, private val enableExtendedMode: Boolean = false) {

    private val logger = LogManager.getLogger(ReportContentProvider::class.java)
    private val trialsProvider = TrialsProvider.create(
        report.patientRecord,
        report.treatmentMatch,
        report.reportConfiguration.countryOfReference,
        enableExtendedMode,
        report.reportConfiguration.filterOnSOCExhaustionAndTumorType
    )
    private val treatmentRankingModel = TreatmentRankingModel(EvidenceScoringModel(createScoringConfig()))

    fun provideChapters(): List<ReportChapter> {
        if (enableExtendedMode) {
            logger.info("Including trial matching details")
        }
        
        if (report.reportConfiguration.includeSOCLiteratureEfficacyEvidence) {
            logger.info("Including SOC literature details")
        }
        
        val externalTrials = trialsProvider.externalTrials()

        return listOf(
            SummaryChapter(report, this),
            PersonalizedEvidenceChapter(
                report,
                include = report.reportConfiguration.includeSOCLiteratureEfficacyEvidence && report.treatmentMatch.personalizedDataAnalysis != null
            ),
            ResistanceEvidenceChapter(report, include = report.reportConfiguration.includeSOCLiteratureEfficacyEvidence),
            MolecularDetailsChapter(
                report,
                report.reportConfiguration.includeMolecularDetailsChapter,
                report.reportConfiguration.includeRawPathologyReport,
                externalTrials.allFiltered()
            ),
            LongitudinalMolecularHistoryChapter(
                report,
                trialsProvider.evaluableCohortsAndNotIgnore(),
                include = report.reportConfiguration.includeLongitudinalMolecularChapter
            ),
            EfficacyEvidenceChapter(report, include = report.reportConfiguration.includeSOCLiteratureEfficacyEvidence),
            ClinicalDetailsChapter(report, include = report.reportConfiguration.includeClinicalDetailsChapter),
            EfficacyEvidenceDetailsChapter(report, include = report.reportConfiguration.includeSOCLiteratureEfficacyEvidence && enableExtendedMode),
            MolecularEvidenceChapter(
                report,
                treatmentRankingModel.rank(report.patientRecord),
                include = report.reportConfiguration.includeMolecularEvidenceChapter
            ),
            TrialMatchingOtherResultsChapter(
                report,
                externalTrialsOnly = report.reportConfiguration.includeOnlyExternalTrialsInTrialMatching,
                trialsProvider,
                include = report.reportConfiguration.includeTrialMatchingChapter
            ),
            TrialMatchingDetailsChapter(report, include = enableExtendedMode)
        ).filter(ReportChapter::include)
    }

    fun provideSummaryTables(keyWidth: Float, valueWidth: Float): List<TableGenerator> {
        val cohorts = trialsProvider.evaluableCohortsAndNotIgnore()
        val clinicalHistoryGenerator = if (report.reportConfiguration.includeOverviewWithClinicalHistorySummary) {
            PatientClinicalHistoryWithOverviewGenerator(
                report = report,
                cohorts = cohorts,
                keyWidth = keyWidth,
                valueWidth = valueWidth
            )
        } else {
            PatientClinicalHistoryGenerator(report = report, showDetails = false, keyWidth = keyWidth, valueWidth = valueWidth)
        }

        val trialTableGenerators = createTrialTableGenerators(
            cohorts = cohorts,
            externalTrials = trialsProvider.externalTrials(),
            requestingSource = TrialSource.fromDescription(report.reportConfiguration.hospitalOfReference)
        ).filterNotNull()

        val approvedTreatmentsGenerator = EligibleApprovedTreatmentGenerator(report)

        return listOfNotNull(
            clinicalHistoryGenerator,
            MolecularSummaryGenerator(
                patientRecord = report.patientRecord,
                cohorts = cohorts,
                keyWidth = keyWidth,
                valueWidth = valueWidth,
                molecularTestFilter = MolecularTestFilter(report.treatmentMatch.maxMolecularTestAge, true)
            ).takeIf {
                report.reportConfiguration.molecularSummaryType != MolecularSummaryType.NONE &&
                        report.patientRecord.molecularTests.isNotEmpty()
            },
            SOCEligibleApprovedTreatmentGenerator(report).takeIf { report.reportConfiguration.includeEligibleSOCTreatmentSummary },
            approvedTreatmentsGenerator.takeIf { report.reportConfiguration.includeApprovedTreatmentsInSummary && approvedTreatmentsGenerator.showTable() }
        ) + trialTableGenerators
    }

    fun provideClinicalDetailsTables(keyWidth: Float, valueWidth: Float): List<TableGenerator> {
        return listOfNotNull(
            PatientClinicalHistoryGenerator(report = report, showDetails = true, keyWidth = keyWidth, valueWidth = valueWidth),
            PatientCurrentDetailsGenerator(
                record = report.patientRecord,
                keyWidth = keyWidth,
                valueWidth = valueWidth,
                referenceDate = report.treatmentMatch.referenceDate
            ),
            TumorDetailsGenerator(record = report.patientRecord, keyWidth = keyWidth, valueWidth = valueWidth),
            report.patientRecord.medications?.let {
                MedicationGenerator(
                    medications = it,
                    interpreter = MedicationStatusInterpreterOnEvaluationDate(report.treatmentMatch.referenceDate, null)
                )
            },
            if (report.patientRecord.bloodTransfusions.isEmpty()) null else
                BloodTransfusionGenerator(bloodTransfusions = report.patientRecord.bloodTransfusions)
        )
    }

    private fun createTrialTableGenerators(
        cohorts: List<InterpretedCohort>,
        externalTrials: ExternalTrials,
        requestingSource: TrialSource?
    ): List<TrialTableGenerator?> {
        val localOpenCohortsGenerator =
            EligibleTrialGenerator.localOpenCohorts(cohorts, externalTrials, requestingSource, report.reportConfiguration.countryOfReference)

        val localOpenCohortsWithMissingMolecularResultForEvaluationGenerator =
            EligibleTrialGenerator.forOpenCohortsWithMissingMolecularResultsForEvaluation(cohorts, requestingSource)

        val nonLocalTrialGenerator = EligibleTrialGenerator.nonLocalOpenCohorts(externalTrials, requestingSource)
        
        return listOfNotNull(
            localOpenCohortsGenerator.takeIf { report.reportConfiguration.includeTrialMatchingInSummary },
            localOpenCohortsWithMissingMolecularResultForEvaluationGenerator.takeIf {
                report.reportConfiguration.includeTrialMatchingInSummary && it?.cohortSize() != 0
            },
            nonLocalTrialGenerator.takeIf { report.reportConfiguration.includeExternalTrialsInSummary && externalTrials.internationalTrials.isNotEmpty() },
        )
    }
}