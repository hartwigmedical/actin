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
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
import com.hartwig.actin.report.trial.SummarizedExternalTrials
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

        val summarizedExternalTrials = trialsProvider.summarizeExternalTrials()

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
                summarizedExternalTrials.allFiltered()
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
            TrialMatchingChapter(
                report,
                report.config.includeIneligibleTrialsInSummary,
                externalTrialsOnly = report.config.includeOnlyExternalTrialsInTrialMatching,
                trialsProvider,
                include = report.config.includeTrialMatchingChapter
            ),
            TrialMatchingDetailsChapter(report, include = includeTrialMatchingDetailsChapter)
        ).filter(ReportChapter::include)
    }

    fun provideClinicalDetailsTables(keyWidth: Float, valueWidth: Float, contentWidth: Float): List<TableGenerator> {
        val bloodTransfusions = report.patientRecord.bloodTransfusions

        return listOfNotNull(
            PatientClinicalHistoryGenerator(report, true, keyWidth, valueWidth),
            PatientCurrentDetailsGenerator(
                report.patientRecord, keyWidth, valueWidth, report.treatmentMatch.referenceDate
            ),
            TumorDetailsGenerator(report.patientRecord, keyWidth, valueWidth),
            report.patientRecord.medications?.let {
                MedicationGenerator(
                    it, contentWidth, MedicationStatusInterpreterOnEvaluationDate(report.treatmentMatch.referenceDate, null)
                )
            },
            if (bloodTransfusions.isEmpty()) null else BloodTransfusionGenerator(bloodTransfusions, contentWidth)
        )
    }

    fun provideSummaryTables(
        keyWidth: Float,
        valueWidth: Float,
        contentWidth: Float,
        interpretedCohorts: List<InterpretedCohort>
    ): List<TableGenerator> {
        val clinicalHistoryGenerator = if (report.config.includeOverviewWithClinicalHistorySummary) {
            PatientClinicalHistoryWithOverviewGenerator(report, interpretedCohorts, keyWidth, valueWidth)
        } else {
            PatientClinicalHistoryGenerator(report, false, keyWidth, valueWidth)
        }

        val trialTableGenerators = getGenerators(
            interpretedCohorts,
            trialsProvider.summarizeExternalTrials(),
            TrialSource.fromDescription(report.requestingHospital),
            contentWidth
        ).filterNotNull()

        return listOfNotNull(
            clinicalHistoryGenerator, MolecularSummaryGenerator(
                report.patientRecord,
                interpretedCohorts,
                keyWidth,
                valueWidth,
                report.config.molecularSummaryType == MolecularSummaryType.SHORT,
                MolecularTestFilter(report.treatmentMatch.maxMolecularTestAge, true)
            ).takeIf {
                report.config.molecularSummaryType != MolecularSummaryType.NONE && report.patientRecord.molecularHistory.molecularTests.isNotEmpty()
            }, SOCEligibleApprovedTreatmentGenerator(report, contentWidth).takeIf {
                report.config.includeEligibleSOCTreatmentSummary
            }, EligibleApprovedTreatmentGenerator(report, contentWidth).takeIf {
                report.config.includeApprovedTreatmentsInSummary
            }) + trialTableGenerators
    }

    private fun getGenerators(
        interpretedCohorts: List<InterpretedCohort>,
        externalTrialSummary: SummarizedExternalTrials,
        requestingSource: TrialSource?,
        contentWidth: Float
    ): List<ActinTrialsGenerator?> {
        val homeCountry = report.config.countryOfReference
        val openCohortsGenerator = EligibleTrialsGenerator.forOpenCohorts(
            interpretedCohorts,
            externalTrialSummary.nationalTrials.filtered.takeIf { report.config.includeExternalTrialsInSummary }.orEmpty(),
            requestingSource,
            homeCountry,
            contentWidth
        )
        val openCohortsWithMissingMolecularResultForEvaluationGenerator =
            EligibleTrialsGenerator.forOpenCohortsWithMissingMolecularResultsForEvaluation(
                interpretedCohorts,
                requestingSource,
                contentWidth
            )
        val nonLocalExternalTrialGenerator = EligibleTrialsGenerator.forOpenCohorts(
            emptyList(),
            externalTrialSummary.internationalTrials.filtered,
            requestingSource,
            homeCountry,
            contentWidth
        )
        val ineligibleTrialGenerator = IneligibleActinTrialsGenerator.forCohorts(
            interpretedCohorts,
            requestingSource,
            contentWidth,
            openOnly = true
        )

        val generators = listOfNotNull(
            openCohortsGenerator.takeIf { report.config.includeTrialMatchingInSummary },
            openCohortsWithMissingMolecularResultForEvaluationGenerator.takeIf {
                report.config.includeTrialMatchingInSummary && (it?.getCohortSize()?.let { size -> size > 0 } ?: true)
            },
            nonLocalExternalTrialGenerator.takeIf { report.config.includeExternalTrialsInSummary },
            ineligibleTrialGenerator.takeIf { report.config.includeIneligibleTrialsInSummary }
        )
        return generators
    }
}