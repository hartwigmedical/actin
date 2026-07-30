package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.configuration.ExternalTrialTumorType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrials
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.layout.element.Table

class EligibleTrialGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val externalTrials: Set<ExternalTrialSummary>,
    private val requestingSource: TrialSource?,
    private val countryOfReference: Country?,
    private val title: String,
    private val footNote: String?,
    private val indicateNoSlotsOrClosed: Boolean,
    private val useSmallerSize: Boolean,
    private val includeWarningsColumn: Boolean,
    private val labels: ReportLabels
) : TrialTableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table =
            if (includeWarningsColumn) {
                Tables.createRelativeWidthCols(1f, 2f, 1f, 1f, 3f)
            } else {
                Tables.createRelativeWidthCols(1f, 1f, 1f, 2f)
            }

        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colTrial()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colCohort()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colMolecular()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colSites()))
        if (includeWarningsColumn) {
            table.addHeaderCell(Cells.createHeader(labels.trialMatching.colWarnings()))
        }

        addTrialsToTable(
            table = table,
            cohorts = cohorts,
            externalTrials = externalTrials,
            requestingSource = requestingSource,
            countryOfReference = countryOfReference,
            includeFeedback = includeWarningsColumn,
            feedbackFunction = InterpretedCohort::warnings,
            indicateNoSlotsOrClosed = indicateNoSlotsOrClosed,
            useSmallerSize = useSmallerSize,
            includeCohortConfig = false,
            includeSites = true
        )
        return table
    }

    override fun footnote(): String? {
        return footNote
    }

    override fun cohortSize(): Int {
        return cohorts.size
    }

    companion object {

        fun localAndNationalExternalOpenAndEligibleCohorts(
            cohorts: List<InterpretedCohort>,
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?,
            countryOfReference: Country?,
            localTrialsType: LocalTrialsType,
            effectiveDutchExternalTrialExclusion: ExternalTrialTumorType,
            labels: ReportLabels
        ): TrialTableGenerator {
            val nationalExternalTrials = ExternalTrialSummarizer.summarize(externalTrials.nationalTrials.filtered)
            val nationalExternalTrialFilteredCount = ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials()).size

            return forLocalAndNationalExternalOpenAndEligibleLocalCohorts(
                openAndEligibleLocalCohorts = filterOpenAndEligibleCohorts(localTrialsType, cohorts),
                relevantNationalExternalTrials = nationalExternalTrials,
                relevantNationalExternalTrialsFilteredCount = nationalExternalTrialFilteredCount,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                trialDescriptionString = trialDescriptionString(localTrialsType, countryOfReference, labels),
                effectiveDutchExternalTrialExclusion = effectiveDutchExternalTrialExclusion,
                labels = labels
            )
        }

        fun externalOpenAndEligibleCohorts(
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?,
            isNational: Boolean,
            effectiveDutchExternalTrialExclusion: ExternalTrialTumorType,
            labels: ReportLabels
        ): TrialTableGenerator {
            val (includedTrials, excludedTrials) = if (isNational) {
                externalTrials.nationalTrials.filtered to externalTrials.excludedNationalTrials()
            } else {
                externalTrials.internationalTrials.filtered to externalTrials.excludedInternationalTrials()
            }
            val relevantExternalTrials = ExternalTrialSummarizer.summarize(includedTrials)
            val relevantExternalTrialsFilteredCount = ExternalTrialSummarizer.summarize(excludedTrials).size

            return forExternalOpenAndEligibleCohorts(
                externalTrials = relevantExternalTrials,
                externalTrialsFilteredCount = relevantExternalTrialsFilteredCount,
                requestingSource = requestingSource,
                isNational = isNational,
                effectiveDutchExternalTrialExclusion = effectiveDutchExternalTrialExclusion,
                labels = labels
            )
        }

        private fun forLocalAndNationalExternalOpenAndEligibleLocalCohorts(
            openAndEligibleLocalCohorts: List<InterpretedCohort>,
            relevantNationalExternalTrials: Set<ExternalTrialSummary>,
            relevantNationalExternalTrialsFilteredCount: Int,
            requestingSource: TrialSource?,
            countryOfReference: Country? = null,
            trialDescriptionString: String,
            effectiveDutchExternalTrialExclusion: ExternalTrialTumorType,
            labels: ReportLabels
        ): TrialTableGenerator {
            val openAndEligibleTrials = openAndEligibleLocalCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsFromTrialsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                openAndEligibleLocalCohorts.size + relevantNationalExternalTrials.size,
                openAndEligibleTrials.size + relevantNationalExternalTrials.size,
                labels
            )
            val title = labels.trialMatching.titleOpenEligible(trialDescriptionString, cohortsFromTrialsString)

            val filteredSuffix = labels.trialMatching.footnoteFilteredSuffix()
            val footNote = if (effectiveDutchExternalTrialExclusion == ExternalTrialTumorType.LUNG) {
                relevantNationalExternalTrialsFilteredCount.takeIf { it > 0 }?.let { count ->
                    labels.trialMatching.footnoteDutchLung(TrialFormatFunctions.formatCountWithLabel(count, labels.misc.trial()), filteredSuffix)
                }
            } else {
                listOfNotNull(
                    labels.trialMatching.footnoteExternalMatched().takeIf { relevantNationalExternalTrials.isNotEmpty() },
                    relevantNationalExternalTrialsFilteredCount.takeIf { it > 0 }?.let { count ->
                        labels.trialMatching.footnoteChildrensHospital(TrialFormatFunctions.formatCountWithLabel(count, labels.misc.trial()), filteredSuffix)
                    }
                ).joinToString("\n").ifEmpty { null }
            }

            return EligibleTrialGenerator(
                cohorts = openAndEligibleLocalCohorts,
                externalTrials = relevantNationalExternalTrials,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                title = title,
                footNote = footNote,
                indicateNoSlotsOrClosed = true,
                useSmallerSize = false,
                includeWarningsColumn = true,
                labels = labels
            )
        }

        private fun filterOpenAndEligibleCohorts(type: LocalTrialsType, cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return TrialsProvider.filterCohortsOpenAndEligible(
                when (type) {
                    LocalTrialsType.LOCAL_LATE_PHASE -> cohorts.filter { it.phase?.isLatePhase == true }
                    LocalTrialsType.LOCAL_EARLY_PHASE -> cohorts.filterNot { it.phase?.isLatePhase == true }
                }
            )
        }

        private fun forExternalOpenAndEligibleCohorts(
            externalTrials: Set<ExternalTrialSummary>,
            externalTrialsFilteredCount: Int,
            requestingSource: TrialSource?,
            isNational: Boolean,
            effectiveDutchExternalTrialExclusion: ExternalTrialTumorType,
            labels: ReportLabels
        ): TrialTableGenerator {
            val cohortsFromTrialsString =
                TrialFormatFunctions.generateCohortsFromTrialsString(externalTrials.size, externalTrials.size, labels)
            val nationalString = if (isNational) labels.trialMatching.phaseNational() else labels.trialMatching.phaseInternational()
            val title = labels.trialMatching.titleOpenEligible(nationalString, cohortsFromTrialsString)

            val filteredSuffix = labels.trialMatching.footnoteFilteredSuffix()
            val footNote =
                if (effectiveDutchExternalTrialExclusion == ExternalTrialTumorType.LUNG && isNational) {
                    externalTrialsFilteredCount.takeIf { it > 0 }?.let { count ->
                        labels.trialMatching.footnoteDutchLung(TrialFormatFunctions.formatCountWithLabel(count, labels.misc.trial()), filteredSuffix)
                    }
                } else {
                    listOfNotNull(
                        labels.trialMatching.footnoteExternalExcluded().takeIf { externalTrials.isNotEmpty() },
                        externalTrialsFilteredCount.takeIf { it > 0 && isNational }?.let { count ->
                            labels.trialMatching.footnoteChildrensHospital(TrialFormatFunctions.formatCountWithLabel(count, labels.misc.trial()), filteredSuffix)
                        },
                        externalTrialsFilteredCount.takeIf { it > 0 && !isNational }?.let { count ->
                            labels.trialMatching.footnoteNationalMolecular(TrialFormatFunctions.formatCountWithLabel(count, labels.misc.trial()), filteredSuffix)
                        }
                    ).joinToString("\n").ifEmpty { null }
                }

            return EligibleTrialGenerator(
                cohorts = emptyList(),
                externalTrials = externalTrials,
                requestingSource = requestingSource,
                countryOfReference = null,
                title = title,
                footNote = footNote,
                indicateNoSlotsOrClosed = false,
                useSmallerSize = false,
                includeWarningsColumn = false,
                labels = labels
            )
        }

        fun openCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>,
            countryOfReference: Country?,
            requestingSource: TrialSource?,
            labels: ReportLabels
        ): TrialTableGenerator? {
            val openAndEligibleButMissingMolecularResultCohorts = filterCohortsOpenAndEligibleButMissingMolecularResult(cohorts)
            val openAndEligibleButMissingMolecularResultTrials =
                openAndEligibleButMissingMolecularResultCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsFromTrialsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                openAndEligibleButMissingMolecularResultCohorts.size,
                openAndEligibleButMissingMolecularResultTrials.size,
                labels
            )

            val countryString = countryOfReferenceString(countryOfReference)
            val title = labels.trialMatching.titleOpenMissingMolecular(countryString, cohortsFromTrialsString)

            return if (openAndEligibleButMissingMolecularResultCohorts.isNotEmpty()) {
                EligibleTrialGenerator(
                    cohorts = openAndEligibleButMissingMolecularResultCohorts,
                    externalTrials = emptySet(),
                    requestingSource = requestingSource,
                    countryOfReference = null,
                    title = title,
                    footNote = null,
                    indicateNoSlotsOrClosed = true,
                    useSmallerSize = false,
                    includeWarningsColumn = true,
                    labels = labels
                )
            } else null
        }

        fun closedCohorts(cohorts: List<InterpretedCohort>, requestingSource: TrialSource?, labels: ReportLabels): TrialTableGenerator {
            val unavailableAndEligibleCohorts = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val unavailableAndEligibleTrials = unavailableAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                unavailableAndEligibleCohorts.size,
                unavailableAndEligibleTrials.size,
                labels
            )

            return EligibleTrialGenerator(
                cohorts = unavailableAndEligibleCohorts,
                externalTrials = emptySet(),
                requestingSource = requestingSource,
                countryOfReference = null,
                title = labels.trialMatching.titleClosedEligible(cohortsString),
                footNote = null,
                indicateNoSlotsOrClosed = false,
                useSmallerSize = true,
                includeWarningsColumn = true,
                labels = labels
            )
        }

        fun filteredExternalTrials(
            externalTrials: ExternalTrials,
            countryOfReference: Country,
            labels: ReportLabels
        ): TrialTableGenerator? {
            val summarizedTrials =
                ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials() + externalTrials.excludedInternationalTrials())
            val title =
                labels.trialMatching.titleFilteredEligible(TrialFormatFunctions.formatCountWithLabel(summarizedTrials.size, labels.misc.trial()))
            return if (summarizedTrials.isNotEmpty()) {
                EligibleTrialGenerator(
                    cohorts = emptyList(),
                    externalTrials = summarizedTrials,
                    requestingSource = null,
                    countryOfReference = countryOfReference,
                    title = title,
                    footNote = null,
                    indicateNoSlotsOrClosed = false,
                    useSmallerSize = true,
                    includeWarningsColumn = false,
                    labels = labels
                )
            } else null
        }

        private fun filterCohortsOpenAndEligibleButMissingMolecularResult(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter { it.isPotentiallyEligible && it.isOpen && it.isMissingMolecularResultForEvaluation }
        }

        private fun countryOfReferenceString(countryOfReference: Country?): String {
            return countryOfReference?.let { "Trials in ${it.display()}" } ?: "Trials"
        }

        private fun trialDescriptionString(type: LocalTrialsType, countryOfReference: Country?, labels: ReportLabels): String {
            val referenceCountryString = countryOfReferenceString(countryOfReference).replaceFirstChar { it.lowercase() }
            return when (type) {
                LocalTrialsType.LOCAL_LATE_PHASE -> labels.trialMatching.phaseLate(referenceCountryString)
                LocalTrialsType.LOCAL_EARLY_PHASE -> labels.trialMatching.phaseEarly(referenceCountryString)
            }
        }
    }
}
