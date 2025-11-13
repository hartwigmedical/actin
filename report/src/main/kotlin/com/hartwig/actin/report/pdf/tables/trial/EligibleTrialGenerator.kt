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

        fun nationalOpenCohorts(
            cohorts: List<InterpretedCohort>,
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?,
            countryOfReference: Country?,
            trialType: TrialType
        ): TrialTableGenerator {
            val nationalExternalTrials = ExternalTrialSummarizer.summarize(externalTrials.nationalTrials.filtered)
            val nationalExternalTrialFilteredCount = ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials()).size

            return forOpenCohorts(
                cohorts = cohorts,
                externalTrials = nationalExternalTrials,
                externalFilteredCount = nationalExternalTrialFilteredCount,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                type = trialType
            )
        }

        fun internationalExternalOpenCohorts(
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?
        ): TrialTableGenerator {
            val internationalExternalTrials = ExternalTrialSummarizer.summarize(externalTrials.internationalTrials.filtered)
            val internationalExternalTrialFilteredCount = ExternalTrialSummarizer.summarize(externalTrials.excludedInternationalTrials()).size

            return forOpenCohorts(
                cohorts = emptyList(),
                externalTrials = internationalExternalTrials,
                externalFilteredCount = internationalExternalTrialFilteredCount,
                requestingSource = requestingSource,
                countryOfReference = null,
                type = TrialType.EXTERNAL
            )
        }

        private fun forOpenCohorts(
            cohorts: List<InterpretedCohort>,
            externalTrials: Set<ExternalTrialSummary>,
            externalFilteredCount: Int,
            requestingSource: TrialSource?,
            countryOfReference: Country? = null,
            type: TrialType
        ): TrialTableGenerator {
            val openAndEligibleCohorts = TrialsProvider.filterCohortsOpenAndEligible(
                when (type) {
                    TrialType.LOCAL_LATE_PHASE -> filterCohortsLateTrialPhase(cohorts)
                    TrialType.LOCAL_EARLY_PHASE -> filterCohortsEarlyTrialPhase(cohorts)
                    else -> cohorts
                }
            )
            val recruitingAndEligibleTrials = openAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()

            val referenceCountryString = countryOfReference?.let { "trials in ${it.display()}" } ?: "trials"
            val trialDescriptionString = when (type) {
                TrialType.LOCAL_LATE_PHASE -> "Phase 2/3 $referenceCountryString"
                TrialType.LOCAL_EARLY_PHASE -> "Phase 1 (or unknown phase) $referenceCountryString"
                TrialType.EXTERNAL -> "International trials"
            }
            val cohortsFromTrialsString = if (openAndEligibleCohorts.isNotEmpty() || externalTrials.isNotEmpty()) {
                "(${
                    formatCountWithLabel(
                        openAndEligibleCohorts.size + externalTrials.size,
                        "cohort"
                    )
                } " + "from ${formatCountWithLabel(recruitingAndEligibleTrials.size + externalTrials.size, "trial")})"
            } else {
                "(0)"
            }
            val title = "$trialDescriptionString that are open and potentially eligible $cohortsFromTrialsString"

            val footNote = if (type != TrialType.EXTERNAL) {
                listOfNotNull(
                    "Trials matched solely on molecular event and tumor type (no clinical data used) are shown in italicized, smaller font."
                        .takeIf { externalTrials.isNotEmpty() },
                    ("${formatCountWithLabel(externalFilteredCount, "trial")} filtered because trial is for young adult patients. " +
                            "See Other Trial Matching Results for filtered matches.").takeIf { externalFilteredCount > 0 }
                ).joinToString("\n")
            } else
                listOfNotNull(
                    "International trials are matched solely on molecular event and tumor type (clinical data excluded)."
                        .takeIf { externalTrials.isNotEmpty() },
                    ("${formatCountWithLabel(externalFilteredCount, "trial")} filtered due to trials recruiting nationally for the same " +
                            "molecular target. See Other Trial Matching Results for filtered matches.")
                        .takeIf { externalFilteredCount > 0 }
                ).joinToString("\n")

            return EligibleTrialGenerator(
                cohorts = openAndEligibleCohorts,
                externalTrials = externalTrials,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                title = title,
                footNote = footNote,
                indicateNoSlotsOrClosed = type != TrialType.EXTERNAL,
                useSmallerSize = false,
                includeWarningsColumn = type != TrialType.EXTERNAL
            )
        }

        fun openCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?
        ): TrialTableGenerator? {
            val recruitingAndEligibleCohorts = filterCohortsOpenAndEligibleButMissingMolecularResult(cohorts)
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty()) {
                val numCohorts = formatCountWithLabel(recruitingAndEligibleCohorts.size, "cohort")
                val numTrials = formatCountWithLabel(recruitingAndEligibleTrials.size, "trial")
                "($numCohorts from $numTrials)"
            } else "(0)"

            val title = "Trials in NL that are open but additional molecular tests needed to evaluate eligibility $cohortFromTrialsText"

            return if (recruitingAndEligibleCohorts.isNotEmpty()) {
                EligibleTrialGenerator(
                    cohorts = recruitingAndEligibleCohorts,
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
            val unavailableAndEligible = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val title = "Trials and cohorts that are potentially eligible, but are closed (${unavailableAndEligible.size})"

            return EligibleTrialGenerator(
                cohorts = unavailableAndEligible,
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
                "Filtered trials potentially eligible based on molecular results which are potentially recruiting (${summarizedTrials.size})"
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
            return cohorts.filter { it !in filterCohortsLateTrialPhase(cohorts) }
        }

        private fun filterCohortsOpenAndEligibleButMissingMolecularResult(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter { it.isPotentiallyEligible && it.isOpen && it.isMissingMolecularResultForEvaluation }
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }
    }
}