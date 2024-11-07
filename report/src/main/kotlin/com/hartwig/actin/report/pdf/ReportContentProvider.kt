package com.hartwig.actin.report.pdf

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.configuration.MolecularSummaryType
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.NO_EVIDENCE_SOURCE
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
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
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummary
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.filterExclusivelyInChildrensHospitals
import com.hartwig.actin.report.pdf.tables.trial.filterInCountryOfReference
import com.hartwig.actin.report.pdf.tables.trial.filterInternalTrials
import com.hartwig.actin.report.pdf.tables.trial.filterMolecularCriteriaAlreadyPresent
import com.hartwig.actin.report.pdf.tables.trial.filterNotInCountryOfReference
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
                report,
                include = report.config.includeSOCLiteratureEfficacyEvidence && report.treatmentMatch.personalizedDataAnalysis != null
            ),
            ResistanceEvidenceChapter(report, include = report.config.includeSOCLiteratureEfficacyEvidence),
            MolecularDetailsChapter(report, report.config.includeMolecularDetailsChapter, report.config.includeRawPathologyReport),
            LongitudinalMolecularHistoryChapter(report, include = report.config.includeLongitudinalMolecularChapter),
            EfficacyEvidenceChapter(report, include = report.config.includeSOCLiteratureEfficacyEvidence),
            ClinicalDetailsChapter(report, include = report.config.includeClinicalDetailsChapter),
            EfficacyEvidenceDetailsChapter(report, include = includeEfficacyEvidenceDetailsChapter),
            MolecularEvidenceChapter(report, include = report.config.includeMolecularEvidenceChapter),
            TrialMatchingChapter(
                report,
                enableExtendedMode,
                report.config.includeIneligibleTrialsInSummary,
                externalTrialsOnly = report.config.includeOnlyExternalTrialsInTrialMatching,
                this,
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

    fun provideSummaryTables(keyWidth: Float, valueWidth: Float, contentWidth: Float): List<TableGenerator> {
        val cohorts =
            InterpretedCohortFactory.createEvaluableCohorts(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)

        val clinicalHistoryGenerator = if (report.config.includeOverviewWithClinicalHistorySummary) {
            PatientClinicalHistoryWithOverviewGenerator(report, cohorts, keyWidth, valueWidth)
        } else {
            PatientClinicalHistoryGenerator(report, false, keyWidth, valueWidth)
        }

        val (openCohortsWithSlotsGenerator, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = true)
        val (openCohortsWithoutSlotsGenerator, _) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = false)
        val cohortsWithMissingGenesGenerator =
            EligibleActinTrialsGenerator.forOpenCohortsWithMissingGenes(cohorts, report.treatmentMatch.trialSource, contentWidth)

        val (localTrialGenerator, nonLocalTrialGenerator) = provideExternalTrialsTables(report.patientRecord, evaluated, contentWidth)
        return listOfNotNull(
            clinicalHistoryGenerator,
            MolecularSummaryGenerator(
                report.patientRecord,
                cohorts,
                keyWidth,
                valueWidth,
                report.config.molecularSummaryType == MolecularSummaryType.SHORT,
                MolecularTestFilter(report.treatmentMatch.maxMolecularTestAge, true)
            ).takeIf {
                report.config.molecularSummaryType != MolecularSummaryType.NONE && report.patientRecord.molecularHistory.molecularTests.isNotEmpty()
            },
            SOCEligibleApprovedTreatmentGenerator(report, contentWidth).takeIf {
                report.config.includeEligibleSOCTreatmentSummary
            },
            EligibleApprovedTreatmentGenerator(
                report.patientRecord,
                contentWidth
            ).takeIf {
                report.config.includeApprovedTreatmentsInSummary
            },
            openCohortsWithSlotsGenerator.takeIf {
                report.config.includeTrialMatchingInSummary
            },
            openCohortsWithoutSlotsGenerator.takeIf {
                report.config.includeTrialMatchingInSummary && (it.getCohortSize() > 0 || report.config.includeEligibleButNoSlotsTableIfEmpty)
            },
            cohortsWithMissingGenesGenerator.takeIf {
                report.config.includeTrialMatchingInSummary
            },
            localTrialGenerator.takeIf { report.config.includeExternalTrialsInSummary },
            nonLocalTrialGenerator.takeIf { report.config.includeExternalTrialsInSummary },
            IneligibleActinTrialsGenerator.forOpenCohorts(
                cohorts,
                report.treatmentMatch.trialSource,
                contentWidth,
                enableExtendedMode
            ).takeIf {
                report.config.includeIneligibleTrialsInSummary
            }
        )
    }

    fun provideExternalTrialsTables(
        patientRecord: PatientRecord, evaluated: List<InterpretedCohort>, contentWidth: Float
    ): Pair<TableGenerator?, TableGenerator?> {
        val externalEligibleTrials =
            AggregatedEvidenceFactory.mergeMapsOfSets(patientRecord.molecularHistory.molecularTests.map {
                AggregatedEvidenceFactory.create(it).externalEligibleTrialsPerEvent
            })

        val externalEligibleTrialsFiltered = ExternalTrialSummarizer.summarize(externalEligibleTrials)
            .filterInternalTrials(report.treatmentMatch.trialMatches.toSet())
            .filterExclusivelyInChildrensHospitals()

        val externalEligibleTrialsLocal = filterTrialsForMolecularCriteria(
            evaluated,
            externalEligibleTrialsFiltered.filterInCountryOfReference(report.config.countryOfReference)
        )

        val externalEligibleTrialsNonLocal = filterTrialsForMolecularCriteria(
            evaluated,
            externalEligibleTrialsFiltered.filterNotInCountryOfReference(report.config.countryOfReference)
        )

        val allEvidenceSources =
            patientRecord.molecularHistory.molecularTests.map { it.evidenceSource }.filter { it != NO_EVIDENCE_SOURCE }.toSet()
        return Pair(
            if (externalEligibleTrialsLocal.isNotEmpty()) {
                EligibleExternalTrialsGenerator(
                    allEvidenceSources,
                    externalEligibleTrialsLocal.filtered,
                    contentWidth,
                    externalEligibleTrialsLocal.numFiltered(),
                    report.config.countryOfReference
                )
            } else null,
            if (externalEligibleTrialsNonLocal.isNotEmpty()) {
                EligibleExternalTrialsGenerator(
                    allEvidenceSources,
                    externalEligibleTrialsNonLocal.filtered,
                    contentWidth,
                    externalEligibleTrialsNonLocal.numFiltered()
                )
            } else null
        )
    }

    data class MolecularFilteredExternalTrials(val original: Set<ExternalTrialSummary>, val filtered: Set<ExternalTrialSummary>) {
        fun numFiltered() = original.size - filtered.size
        fun isNotEmpty() = filtered.isNotEmpty()
    }

    private fun filterTrialsForMolecularCriteria(
        internalEvaluatedCohorts: List<InterpretedCohort>,
        original: Set<ExternalTrialSummary>
    ): MolecularFilteredExternalTrials {
        return MolecularFilteredExternalTrials(original, original.filterMolecularCriteriaAlreadyPresent(internalEvaluatedCohorts))
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ReportContentProvider::class.java)
    }
}