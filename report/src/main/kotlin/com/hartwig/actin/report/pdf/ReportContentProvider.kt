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
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
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
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.trial.ExternalTrials
import com.hartwig.actin.report.trial.TrialsProvider
import org.apache.logging.log4j.LogManager

class ReportContentProvider(private val report: Report, private val enableExtendedMode: Boolean = false) {

    private val logger = LogManager.getLogger(ReportContentProvider::class.java)
    private val trialsProvider = TrialsProvider(
        report.patientRecord,
        report.treatmentMatch,
        report.config.countryOfReference,
        enableExtendedMode,
        report.config.filterOnSOCExhaustionAndTumorType
    )

    fun provideChapters(): List<ReportChapter> {
        val (includeEfficacyEvidenceDetailsChapter, includeTrialMatchingDetailsChapter) = when {
            !enableExtendedMode -> {
                Pair(false, false)
            }

            report.config.includeSOCLiteratureEfficacyEvidence -> {
                logger.info("Including SOC literature details")
                Pair(true, false)
            }

            else -> {
                logger.info("Including trial matching details")
                Pair(false, true)
            }
        }

        val summarizedExternalTrials = trialsProvider.externalTrials()

        return listOf(
            SummaryChapter(report, this, trialsProvider.evaluableCohortsAndNotIgnore()),
            PersonalizedEvidenceChapter(
                report,
                include = report.config.includeSOCLiteratureEfficacyEvidence && report.treatmentMatch.personalizedDataAnalysis != null
            ),
            ResistanceEvidenceChapter(report, include = report.config.includeSOCLiteratureEfficacyEvidence),
            MolecularDetailsChapter(
                report,
                report.config.includeMolecularDetailsChapter,
                report.config.includeRawPathologyReport,
                ExternalTrialSummarizer.summarize(summarizedExternalTrials.allFiltered())
            ),
            LongitudinalMolecularHistoryChapter(
                report,
                trialsProvider.evaluableCohortsAndNotIgnore(),
                include = report.config.includeLongitudinalMolecularChapter
            ),
            EfficacyEvidenceChapter(report, include = report.config.includeSOCLiteratureEfficacyEvidence),
            ClinicalDetailsChapter(report, include = report.config.includeClinicalDetailsChapter),
            EfficacyEvidenceDetailsChapter(report, include = includeEfficacyEvidenceDetailsChapter),
            MolecularEvidenceChapter(report, include = report.config.includeMolecularEvidenceChapter),
            TrialMatchingOtherResultsChapter(
                report,
                report.config.includeIneligibleTrialsInSummary,
                externalTrialsOnly = report.config.includeOnlyExternalTrialsInTrialMatching,
                trialsProvider,
                include = report.config.includeTrialMatchingChapter
            ),
            TrialMatchingDetailsChapter(report, include = includeTrialMatchingDetailsChapter)
        ).filter(ReportChapter::include)
    }

    fun provideSummaryTables(keyWidth: Float, valueWidth: Float, cohorts: List<InterpretedCohort>): List<TableGenerator> {
        val clinicalHistoryGenerator = if (report.config.includeOverviewWithClinicalHistorySummary) {
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
            externalTrialSummary = trialsProvider.externalTrials(),
            requestingSource = TrialSource.fromDescription(report.requestingHospital)
        ).filterNotNull()

        return listOfNotNull(
            clinicalHistoryGenerator,
            MolecularSummaryGenerator(
                patientRecord = report.patientRecord,
                cohorts = cohorts,
                keyWidth = keyWidth,
                valueWidth = valueWidth,
                isShort = report.config.molecularSummaryType == MolecularSummaryType.SHORT,
                molecularTestFilter = MolecularTestFilter(report.treatmentMatch.maxMolecularTestAge, true)
            ).takeIf {
                report.config.molecularSummaryType != MolecularSummaryType.NONE &&
                        report.patientRecord.molecularHistory.molecularTests.isNotEmpty()
            },
            SOCEligibleApprovedTreatmentGenerator(report).takeIf { report.config.includeEligibleSOCTreatmentSummary },
            EligibleApprovedTreatmentGenerator(report).takeIf { report.config.includeApprovedTreatmentsInSummary }
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
        externalTrialSummary: ExternalTrials,
        requestingSource: TrialSource?
    ): List<TrialTableGenerator?> {
        val localExternalTrials = ExternalTrialSummarizer.summarize(
            externalTrialSummary.nationalTrials.filtered.takeIf { report.config.includeExternalTrialsInSummary }.orEmpty()
        )
        val localExternalTrialFilteredCount = externalTrialSummary.excludedNationalTrials()
            .groupBy { ewt -> ewt.trial.nctId }.size.takeIf { report.config.includeExternalTrialsInSummary } ?: 0

        val remoteExternalTrials = ExternalTrialSummarizer.summarize(externalTrialSummary.internationalTrials.filtered)
        val remoteExternalTrialFilteredCount = externalTrialSummary.excludedInternationalTrials().groupBy { ewt -> ewt.trial.nctId }.size

        val localOpenCohortsGenerator = EligibleTrialGenerator.forOpenCohorts(
            cohorts = cohorts,
            externalTrials = localExternalTrials,
            externalFilteredCount = localExternalTrialFilteredCount,
            requestingSource = requestingSource,
            countryOfReference = report.config.countryOfReference,
            forLocalTrials = true
        )
        val localOpenCohortsWithMissingMolecularResultForEvaluationGenerator =
            EligibleTrialGenerator.forOpenCohortsWithMissingMolecularResultsForEvaluation(cohorts, requestingSource)
        val nonLocalTrialGenerator = EligibleTrialGenerator.forOpenCohorts(
            cohorts = emptyList(),
            externalTrials = remoteExternalTrials,
            externalFilteredCount = remoteExternalTrialFilteredCount,
            requestingSource = requestingSource,
            countryOfReference = null,
            forLocalTrials = false
        )
        val ineligibleTrialGenerator = IneligibleTrialGenerator.forEvaluableCohorts(
            cohorts = cohorts,
            requestingSource = requestingSource,
            openOnly = true
        )

        val generators = listOfNotNull(
            localOpenCohortsGenerator.takeIf { report.config.includeTrialMatchingInSummary },
            localOpenCohortsWithMissingMolecularResultForEvaluationGenerator.takeIf {
                report.config.includeTrialMatchingInSummary && it?.cohortSize() != 0
            },
            nonLocalTrialGenerator
                .takeIf { report.config.includeExternalTrialsInSummary && externalTrialSummary.internationalTrials.isNotEmpty() },
            ineligibleTrialGenerator.takeIf { report.config.includeIneligibleTrialsInSummary }
        )
        return generators
    }
}