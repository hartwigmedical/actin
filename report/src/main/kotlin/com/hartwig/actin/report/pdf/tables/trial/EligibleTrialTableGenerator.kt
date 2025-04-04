package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.layout.element.Table

class EligibleTrialTableGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val externalTrials: Set<ExternalTrialSummary>,
    private val requestingSource: TrialSource?,
    private val homeCountry: Country? = null,
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

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(
            trialColWidth, cohortColWidth + molecularEventColWidth + locationColWidth + (checksColWidth ?: 0f)
        )
        val widths = listOfNotNull(cohortColWidth, molecularEventColWidth, locationColWidth, checksColWidth).toFloatArray()

        val headers = sequenceOf("Trial", "Cohort", "Molecular", "Sites", "Warnings".takeIf { cohorts.isNotEmpty() }).filterNotNull()

        if (cohorts.isNotEmpty() || externalTrials.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader(headers.first())))
            val headerSubTable = Tables.createFixedWidthCols(*widths)
            headers.drop(1).map(Cells::createHeader).forEach(headerSubTable::addHeaderCell)
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        addTrialsToTable(
            cohorts = cohorts,
            externalTrials = externalTrials,
            requestingSource = requestingSource,
            homeCountry = homeCountry,
            table = table,
            tableWidths = widths,
            feedbackFunction = InterpretedCohort::warnings,
            allowDeEmphasis = allowDeEmphasis
        )
        if (footNote != null) {
            table.addCell(Cells.createSpanningSubNote(footNote, table))
        }
        return makeWrapping(table)
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
            homeCountry: Country? = null,
            width: Float,
            localTrials: Boolean = true
        ): EligibleTrialTableGenerator {
            val recruitingAndEligibleCohorts = TrialsProvider.filterCohortsAvailable(cohorts)
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty() || externalTrials.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size + externalTrials.size, "cohort")} " +
                        "from ${formatCountWithLabel(recruitingAndEligibleTrials.size + externalTrials.size, "trial")})"
            } else {
                "(0)"
            }
            val locationString = if (!localTrials) "International trials" else homeCountry?.let { "Trials in ${it.display()}" } ?: "Trials"
            val title = "$locationString that are open and potentially eligible $cohortFromTrialsText"
            val filteredNote = if (filteredCount > 0) {
                "\n$filteredCount trials were filtered due to eligible local trials for the same molecular target or trial for young adult patients."
            } else ""
            val externalTrialNote = if (externalTrials.isNotEmpty()) {
                "\nTrials matched solely on molecular event and tumor type (no clinical data used) are shown in italicized, smaller font."
            } else ""
            val footNote = if (localTrials) {
                "Open cohorts with no slots available are shown in grey.$externalTrialNote$filteredNote"
            } else "International trials are matched solely on molecular event and tumor type (clinical data excluded)."

            return create(
                recruitingAndEligibleCohorts,
                externalTrials,
                requestingSource,
                homeCountry,
                title,
                width,
                footNote,
                localTrials,
                localTrials
            )
        }

        fun forOpenCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>, requestingSource: TrialSource?, width: Float
        ): EligibleTrialTableGenerator? {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.isMissingMolecularResultForEvaluation!!
            }
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortFromTrialsText = when {
                recruitingAndEligibleCohorts.isNotEmpty() -> "(${
                    formatCountWithLabel(
                        recruitingAndEligibleCohorts.size,
                        "cohort"
                    )
                }" + " from ${formatCountWithLabel(recruitingAndEligibleTrials.size, "trial")})"

                else -> "(0)"
            }

            val title = "Trials that are open but additional molecular tests needed to evaluate eligibility $cohortFromTrialsText"
            val footNote = "Open cohorts with no slots available are shown in grey."

            return if (recruitingAndEligibleCohorts.isNotEmpty()) {
                create(recruitingAndEligibleCohorts, externalTrials = emptySet(), requestingSource, null, title, width, footNote)
            } else null
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }

        fun forClosedCohorts(
            cohorts: List<InterpretedCohort>, requestingSource: TrialSource?, contentWidth: Float
        ): EligibleTrialTableGenerator {
            val unavailableAndEligible = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val title =
                "Trials and cohorts that are potentially eligible, but are closed (${unavailableAndEligible.size})"
            return create(unavailableAndEligible, emptySet(), requestingSource, null, title, contentWidth, null, false)
        }

        private fun create(
            cohorts: List<InterpretedCohort>,
            externalTrials: Set<ExternalTrialSummary>,
            requestingSource: TrialSource?,
            homeCountry: Country?,
            title: String,
            width: Float,
            footNote: String? = null,
            allowDeEmphasis: Boolean = true,
            includeChecksColumn: Boolean = true
        ): EligibleTrialTableGenerator {
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val locationColWidth = if (includeChecksColumn) width / 7 else width / 2
            val checksColWidth =
                if (includeChecksColumn) width - (trialColWidth + cohortColWidth + molecularColWidth + locationColWidth) else null

            return EligibleTrialTableGenerator(
                cohorts,
                externalTrials,
                requestingSource,
                homeCountry,
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