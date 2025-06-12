package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
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
    private val allowDeEmphasis: Boolean,
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
            allowDeEmphasis = allowDeEmphasis,
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

        fun localOpenCohorts(
            cohorts: List<InterpretedCohort>,
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?,
            countryOfReference: Country?
        ): TrialTableGenerator {
            val localExternalTrials = ExternalTrialSummarizer.summarize(externalTrials.nationalTrials.filtered)
            val localExternalTrialFilteredCount = ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials()).size

            return forOpenCohorts(
                cohorts = cohorts,
                externalTrials = localExternalTrials,
                externalFilteredCount = localExternalTrialFilteredCount,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                forLocalTrials = true
            )
        }

        fun nonLocalOpenCohorts(
            externalTrials: ExternalTrials,
            requestingSource: TrialSource?
        ): TrialTableGenerator {
            val remoteExternalTrials = ExternalTrialSummarizer.summarize(externalTrials.internationalTrials.filtered)
            val remoteExternalTrialFilteredCount = ExternalTrialSummarizer.summarize(externalTrials.excludedInternationalTrials()).size

            return forOpenCohorts(
                cohorts = emptyList(),
                externalTrials = remoteExternalTrials,
                externalFilteredCount = remoteExternalTrialFilteredCount,
                requestingSource = requestingSource,
                countryOfReference = null,
                forLocalTrials = false
            )
        }

        private fun forOpenCohorts(
            cohorts: List<InterpretedCohort>,
            externalTrials: Set<ExternalTrialSummary>,
            externalFilteredCount: Int,
            requestingSource: TrialSource?,
            countryOfReference: Country? = null,
            forLocalTrials: Boolean = true
        ): TrialTableGenerator {
            val recruitingAndEligibleCohorts = TrialsProvider.filterCohortsAvailable(cohorts)
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty() || externalTrials.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size + externalTrials.size, "cohort")} " +
                        "from ${formatCountWithLabel(recruitingAndEligibleTrials.size + externalTrials.size, "trial")})"
            } else {
                "(0)"
            }
            val locationString =
                if (!forLocalTrials) "International trials" else countryOfReference?.let { "Trials in ${it.display()}" } ?: "Trials"
            val title = "$locationString that are open and potentially eligible $cohortFromTrialsText"
            val footNote = if (forLocalTrials) {
                listOfNotNull(
                    "Trials matched solely on molecular event and tumor type (no clinical data used) are shown in italicized, smaller font."
                        .takeIf { externalTrials.isNotEmpty() },
                    ("${formatCountWithLabel(externalFilteredCount, "trial")} filtered due to eligible local trials for the same molecular " +
                            "target and/or the trial is for young adult patients. See Other Trial Matching Results for filtered matches.")
                        .takeIf { externalFilteredCount > 0 }
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
                cohorts = recruitingAndEligibleCohorts,
                externalTrials = externalTrials,
                requestingSource = requestingSource,
                countryOfReference = countryOfReference,
                title = title,
                footNote = footNote,
                allowDeEmphasis = forLocalTrials,
                useSmallerSize = false,
                includeWarningsColumn = forLocalTrials
            )
        }

        fun forOpenCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?
        ): TrialTableGenerator? {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.isMissingMolecularResultForEvaluation
            }
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
                    allowDeEmphasis = true,
                    useSmallerSize = false,
                    includeWarningsColumn = true
                )
            } else null
        }

        fun forClosedCohorts(cohorts: List<InterpretedCohort>, requestingSource: TrialSource?): TrialTableGenerator {
            val unavailableAndEligible = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val title = "Trials and cohorts that are potentially eligible, but are closed (${unavailableAndEligible.size})"

            return EligibleTrialGenerator(
                cohorts = unavailableAndEligible,
                externalTrials = emptySet(),
                requestingSource = requestingSource,
                countryOfReference = null,
                title = title,
                footNote = null,
                allowDeEmphasis = false,
                useSmallerSize = true,
                includeWarningsColumn = true
            )
        }

        fun forFilteredTrials(externalTrials: ExternalTrials, countryOfReference: Country): TrialTableGenerator? {
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
                    allowDeEmphasis = false,
                    useSmallerSize = true,
                    includeWarningsColumn = false
                )
            } else null
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }
    }
}