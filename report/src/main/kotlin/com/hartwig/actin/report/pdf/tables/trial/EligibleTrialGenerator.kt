package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.layout.element.Table

class EligibleTrialGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val externalTrials: Set<ExternalTrialSummary>,
    private val requestingSource: TrialSource?,
    private val countryOfReference: Country?,
    private val title: String,
    private val footNote: String?,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val locationColWidth: Float,
    private val checksColWidth: Float?,
    private val allowDeEmphasis: Boolean
) : TrialTableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val subTableWidths = listOfNotNull(
            cohortColWidth, molecularEventColWidth, locationColWidth, checksColWidth.takeIf { cohorts.isNotEmpty() }
        ).toFloatArray()

        val table = Tables.createFixedWidthCols(trialColWidth, subTableWidths.sum())
        val subTableHeaders = listOfNotNull("Cohort", "Molecular", "Sites", "Warnings".takeIf { cohorts.isNotEmpty() })
        if (cohorts.isNotEmpty() || externalTrials.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val subTable = Tables.createFixedWidthCols(*subTableWidths)
            subTableHeaders.map(Cells::createHeader).forEach(subTable::addHeaderCell)
            table.addHeaderCell(Cells.createContentNoBorder(subTable))
        }

        addTrialsToTable(
            cohorts = cohorts,
            externalTrials = externalTrials,
            requestingSource = requestingSource,
            countryOfReference = countryOfReference,
            table = table,
            tableWidths = subTableWidths,
            feedbackFunction = InterpretedCohort::warnings,
            allowDeEmphasis = allowDeEmphasis
        )
        if (footNote != null) {
            table.addCell(Cells.createSpanningSubNote(footNote, table))
        }
        return table
    }

    override fun getCohortSize(): Int {
        return cohorts.size
    }

    companion object {

        fun forOpenCohorts(
            cohorts: List<InterpretedCohort>,
            externalTrials: Set<ExternalTrialSummary>,
            filteredCount: Int,
            requestingSource: TrialSource?,
            countryOfReference: Country? = null,
            width: Float,
            localTrials: Boolean = true
        ): EligibleTrialGenerator {
            val recruitingAndEligibleCohorts = TrialsProvider.filterCohortsAvailable(cohorts)
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty() || externalTrials.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size + externalTrials.size, "cohort")} " +
                        "from ${formatCountWithLabel(recruitingAndEligibleTrials.size + externalTrials.size, "trial")})"
            } else {
                "(0)"
            }
            val locationString =
                if (!localTrials) "International trials" else countryOfReference?.let { "Trials in ${it.display()}" } ?: "Trials"
            val title = "$locationString that are open and potentially eligible $cohortFromTrialsText"
            val footNote = if (localTrials) {
                listOfNotNull(
                    "Open cohorts with no slots available are shown in grey."
                        .takeIf { recruitingAndEligibleCohorts.any { !it.hasSlotsAvailable } },
                    "Trials matched solely on molecular event and tumor type (no clinical data used) are shown in italicized, smaller font."
                        .takeIf { externalTrials.isNotEmpty() },
                    ("${formatCountWithLabel(filteredCount, "trial")} filtered due to eligible local trials for the same molecular " +
                            "target or because the trial is for young adult patients only.")
                                .takeIf { filteredCount > 0 }
                ).joinToString("\n")
            } else "International trials are matched solely on molecular event and tumor type (clinical data excluded)."

            return create(
                recruitingAndEligibleCohorts,
                externalTrials,
                requestingSource,
                countryOfReference,
                title,
                width,
                footNote,
                localTrials,
                localTrials
            )
        }

        fun forOpenCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>, requestingSource: TrialSource?, width: Float
        ): EligibleTrialGenerator? {
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
            val footNote = "Open cohorts with no slots available are shown in grey."
                .takeUnless { recruitingAndEligibleCohorts.all(InterpretedCohort::hasSlotsAvailable) }

            return if (recruitingAndEligibleCohorts.isNotEmpty()) {
                create(recruitingAndEligibleCohorts, externalTrials = emptySet(), requestingSource, null, title, width, footNote)
            } else null
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }

        fun forClosedCohorts(
            cohorts: List<InterpretedCohort>, requestingSource: TrialSource?, contentWidth: Float
        ): EligibleTrialGenerator {
            val unavailableAndEligible = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val title =
                "Trials and cohorts that are potentially eligible, but are closed (${unavailableAndEligible.size})"
            return create(unavailableAndEligible, emptySet(), requestingSource, null, title, contentWidth, null, allowDeEmphasis = false)
        }

        fun forFilteredTrials(
            trials: Set<ExternalTrialSummary>, countryOfReference: Country, contentWidth: Float
        ): EligibleTrialGenerator {
            val title = "Filtered trials potentially eligible based on molecular results which are potentially recruiting (${trials.size})"
            return create(emptyList(), trials, null, countryOfReference, title, contentWidth, allowDeEmphasis = false, includeChecksColumn = false)
        }

        private fun create(
            cohorts: List<InterpretedCohort>,
            externalTrials: Set<ExternalTrialSummary>,
            requestingSource: TrialSource?,
            countryOfReference: Country?,
            title: String,
            width: Float,
            footNote: String? = null,
            allowDeEmphasis: Boolean = true,
            includeChecksColumn: Boolean = true
        ): EligibleTrialGenerator {
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val locationColWidth = if (includeChecksColumn) width / 7 else width / 2
            val checksColWidth =
                if (includeChecksColumn) width - (trialColWidth + cohortColWidth + molecularColWidth + locationColWidth) else null

            return EligibleTrialGenerator(
                cohorts,
                externalTrials,
                requestingSource,
                countryOfReference,
                title,
                footNote,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                locationColWidth,
                checksColWidth,
                allowDeEmphasis
            )
        }
    }
}