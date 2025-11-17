package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrials
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.layout.element.Table
import kotlin.collections.map

private val TRIAL_LATE_PHASES = setOf(
    TrialPhase.PHASE_2,
    TrialPhase.PHASE_2_OR_MORE,
    TrialPhase.PHASE_2_3,
    TrialPhase.PHASE_3,
    TrialPhase.PHASE_4,
    TrialPhase.COMPASSIONATE_USE,
)

class EligibleTrialGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val externalTrials: Set<ExternalTrialSummary>,
    private val requestingSource: TrialSource?,
    private val countryOfReference: Country?,
    private val title: String,
    private val footNote: String?,
    private val indicateNoSlotsOrClosed: Boolean,
    private val useSmallerSize: Boolean,
    private val includeWarningsColumn: Boolean
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

        table.addHeaderCell(Cells.createHeader("Trial"))
        table.addHeaderCell(Cells.createHeader("Cohort"))
        table.addHeaderCell(Cells.createHeader("Molecular"))
        table.addHeaderCell(Cells.createHeader("Sites"))
        if (includeWarningsColumn) {
            table.addHeaderCell(Cells.createHeader("Warnings"))
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
        if (footNote != null) {
            val note = Cells.createSpanningSubNote(footNote, table).apply {
                if (useSmallerSize) setFontSize(Styles.SMALL_FONT_SIZE)
            }
            table.addCell(note)
        }
        return table
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
            localTrialsType: LocalTrialsType
        ): TrialTableGenerator {
            val nationalExternalTrials = if (localTrialsType == LocalTrialsType.LOCAL_EARLY_PHASE) {
                ExternalTrialSummarizer.summarize(externalTrials.nationalTrials.filtered)
            } else emptySet()
            val nationalExternalTrialFilteredCount = ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials()).size

            return forLocalAndNationalExternalOpenAndEligibleLocalCohorts(
                openAndEligibleLocalCohorts = filterOpenAndEligibleCohorts(localTrialsType, cohorts),
                relevantExternalTrials = nationalExternalTrials,
                relevantExternalTrialsFilteredCount = nationalExternalTrialFilteredCount,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                trialDescriptionString = trialDescriptionString(localTrialsType, countryOfReference),
            )
        }

        fun externalOpenAndEligibleCohorts(
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?,
            isNational: Boolean
        ): TrialTableGenerator {
            val (relevantExternalTrials, relevantExternalTrialsFilteredCount) =
                if (isNational) {
                    ExternalTrialSummarizer.summarize(externalTrials.nationalTrials.filtered) to
                            ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials()).size
                } else {
                    ExternalTrialSummarizer.summarize(externalTrials.internationalTrials.filtered) to
                            ExternalTrialSummarizer.summarize(externalTrials.excludedInternationalTrials()).size
                }

            return forExternalOpenAndEligibleCohorts(
                externalTrials = relevantExternalTrials,
                externalTrialsFilteredCount = relevantExternalTrialsFilteredCount,
                requestingSource = requestingSource,
                isNational = isNational
            )
        }

        private fun forLocalAndNationalExternalOpenAndEligibleLocalCohorts(
            openAndEligibleLocalCohorts: List<InterpretedCohort>,
            relevantExternalTrials: Set<ExternalTrialSummary>,
            relevantExternalTrialsFilteredCount: Int,
            requestingSource: TrialSource?,
            countryOfReference: Country? = null,
            trialDescriptionString: String
        ): TrialTableGenerator {
            val openAndEligibleTrials = openAndEligibleLocalCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsFromTrialsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                openAndEligibleLocalCohorts.size + relevantExternalTrials.size,
                openAndEligibleTrials.size + relevantExternalTrials.size
            )
            val title = "$trialDescriptionString that are open and potentially eligible $cohortsFromTrialsString"

            val footNote = listOfNotNull(
                "Trials matched solely on molecular event and tumor type (no clinical data used) are shown in italicized, smaller font."
                    .takeIf { relevantExternalTrials.isNotEmpty() },
                ("${
                    TrialFormatFunctions.formatCountWithLabel(
                        relevantExternalTrialsFilteredCount,
                        "trial"
                    )
                } filtered because trial is for young adult patients. " +
                        "See Other Trial Matching Results for filtered matches.").takeIf { relevantExternalTrialsFilteredCount > 0 }
            ).joinToString("\n")

            return EligibleTrialGenerator(
                cohorts = openAndEligibleLocalCohorts,
                externalTrials = relevantExternalTrials,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                title = title,
                footNote = footNote,
                indicateNoSlotsOrClosed = true,
                useSmallerSize = false,
                includeWarningsColumn = true
            )
        }

        private fun filterOpenAndEligibleCohorts(type: LocalTrialsType, cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return TrialsProvider.filterCohortsOpenAndEligible(
                when (type) {
                    LocalTrialsType.LOCAL_LATE_PHASE -> filterCohortsLateTrialPhase(cohorts)
                    LocalTrialsType.LOCAL_EARLY_PHASE -> filterCohortsEarlyTrialPhase(cohorts)
                }
            )
        }

        private fun forExternalOpenAndEligibleCohorts(
            externalTrials: Set<ExternalTrialSummary>,
            externalTrialsFilteredCount: Int,
            requestingSource: TrialSource?,
            isNational: Boolean
        ): TrialTableGenerator {
            val cohortsFromTrialsString = TrialFormatFunctions.generateCohortsFromTrialsString(externalTrials.size, externalTrials.size)
            val nationalString = if (isNational) "National" else "International"
            val title = "$nationalString trials that are open and potentially eligible $cohortsFromTrialsString"

            val footNote = listOfNotNull(
                "Trials in this table are matched solely on molecular event and tumor type (clinical data excluded).".takeIf { externalTrials.isNotEmpty() },
                ("${
                    TrialFormatFunctions.formatCountWithLabel(
                        externalTrialsFilteredCount,
                        "trial"
                    )
                } filtered due to trials recruiting nationally for the same " + "molecular target. See Other Trial Matching Results for filtered matches.").takeIf { externalTrialsFilteredCount > 0 }).joinToString(
                "\n"
            )

            return EligibleTrialGenerator(
                cohorts = emptyList(),
                externalTrials = externalTrials,
                requestingSource = requestingSource,
                countryOfReference = null,
                title = title,
                footNote = footNote,
                indicateNoSlotsOrClosed = false,
                useSmallerSize = false,
                includeWarningsColumn = false
            )
        }

        fun openCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>,
            countryOfReference: Country?,
            requestingSource: TrialSource?
        ): TrialTableGenerator? {
            val openAndEligibleButMissingMolecularResultCohorts = filterCohortsOpenAndEligibleButMissingMolecularResult(cohorts)
            val openAndEligibleButMissingMolecularResultTrials =
                openAndEligibleButMissingMolecularResultCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsFromTrialsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                openAndEligibleButMissingMolecularResultCohorts.size,
                openAndEligibleButMissingMolecularResultTrials.size
            )

            val title =
                "${countryOfReferenceString(countryOfReference)} that are open but additional molecular tests needed to evaluate eligibility $cohortsFromTrialsString"

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
                    includeWarningsColumn = true
                )
            } else null
        }

        fun closedCohorts(cohorts: List<InterpretedCohort>, requestingSource: TrialSource?): TrialTableGenerator {
            val unavailableAndEligibleCohorts = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val unavailableAndEligibleTrials = unavailableAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val title = "Trials and cohorts that are potentially eligible, but are closed ${
                TrialFormatFunctions.generateCohortsFromTrialsString(
                    unavailableAndEligibleCohorts.size,
                    unavailableAndEligibleTrials.size
                )
            }"

            return EligibleTrialGenerator(
                cohorts = unavailableAndEligibleCohorts,
                externalTrials = emptySet(),
                requestingSource = requestingSource,
                countryOfReference = null,
                title = title,
                footNote = null,
                indicateNoSlotsOrClosed = false,
                useSmallerSize = true,
                includeWarningsColumn = true
            )
        }

        fun filteredExternalTrials(externalTrials: ExternalTrials, countryOfReference: Country): TrialTableGenerator? {
            val summarizedTrials =
                ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials() + externalTrials.excludedInternationalTrials())
            val title =
                "Filtered trials potentially eligible based on molecular results which are potentially recruiting (${TrialFormatFunctions.formatCountWithLabel(summarizedTrials.size, "trial")})"
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
                    includeWarningsColumn = false
                )
            } else null
        }

        private fun filterCohortsLateTrialPhase(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter { it.phase in TRIAL_LATE_PHASES }
        }

        private fun filterCohortsEarlyTrialPhase(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filterNot { it.phase in TRIAL_LATE_PHASES }
        }

        private fun filterCohortsOpenAndEligibleButMissingMolecularResult(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter { it.isPotentiallyEligible && it.isOpen && it.isMissingMolecularResultForEvaluation }
        }

        private fun countryOfReferenceString(countryOfReference: Country?): String {
            return countryOfReference?.let { "Trials in ${it.display()}" } ?: "Trials"
        }

        private fun trialDescriptionString(type: LocalTrialsType, countryOfReference: Country?): String {
            val referenceCountryString = countryOfReferenceString(countryOfReference).replaceFirstChar { it.lowercase() }
            return when (type) {
                LocalTrialsType.LOCAL_LATE_PHASE -> "Phase 2/3+ $referenceCountryString"
                LocalTrialsType.LOCAL_EARLY_PHASE -> "Phase 1/2 (or unknown phase) $referenceCountryString"
            }
        }
    }
}