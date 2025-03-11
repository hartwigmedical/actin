package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.createTableTitleStart
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.hartwig.actin.report.trial.filterCohortsAvailable
import com.itextpdf.layout.element.Table

class EligibleActinTrialsGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val title: String,
    private val footNote: String?,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val locationColWidth: Float?,
    private val checksColWidth: Float
) : TableGenerator {

    private val includeLocation = locationColWidth != null

    override fun title(): String {
        return title
    }

    override fun contents(): Table {

        val table = Tables.createFixedWidthCols(
            trialColWidth, cohortColWidth + molecularEventColWidth + (locationColWidth ?: 0f) + checksColWidth
        )
        val widths = listOfNotNull(cohortColWidth, molecularEventColWidth, locationColWidth, checksColWidth).toFloatArray()

        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = Tables.createFixedWidthCols(*widths)
            sequenceOf("Cohort", "Molecular", "Hospitals".takeIf { includeLocation }, "Warnings").filterNotNull()
                .map(Cells::createHeader).forEach(headerSubTable::addHeaderCell)
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        addTrialsToTable(
            cohorts = cohorts,
            table = table,
            tableWidths = widths,
            feedbackFunction = InterpretedCohort::warnings,
            includeLocation = includeLocation
        )
        if (footNote != null) {
            table.addCell(Cells.createSpanningSubNote(footNote, table))
        }
        return makeWrapping(table)
    }

    fun getCohortSize(): Int {
        return cohorts.size
    }

    companion object {

        fun forOpenCohorts(
            cohorts: List<InterpretedCohort>, source: String?, width: Float, slotsAvailable: Boolean, includeLocation: Boolean = false
        ): EligibleActinTrialsGenerator {
            val recruitingAndEligibleCohorts = filterCohortsAvailable(cohorts, slotsAvailable)
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val slotsText = if (!slotsAvailable) " but currently have no slots available" else ""
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size, "cohort")} " +
                        "from ${formatCountWithLabel(recruitingAndEligibleTrials.size, "trial")})"
            } else {
                "(0)"
            }

            val title = "${createTableTitleStart(source)} that are open and potentially eligible$slotsText $cohortFromTrialsText"

            return create(recruitingAndEligibleCohorts, title, width, null, includeLocation)
        }

        fun forOpenCohortsWithMissingMolecularResultsForEvaluation(
            cohorts: List<InterpretedCohort>, source: String?, width: Float, includeLocation: Boolean = false
        ): EligibleActinTrialsGenerator? {
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

            val title =
                "${createTableTitleStart(source)} that are open but additional molecular tests needed to evaluate eligibility $cohortFromTrialsText"
            val footNote = "Open cohorts with no slots available are shown in grey."

            return if (recruitingAndEligibleCohorts.isNotEmpty()) create(
                recruitingAndEligibleCohorts, title, width, footNote, includeLocation
            ) else null
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }

        fun forClosedCohorts(
            cohorts: List<InterpretedCohort>, source: String?, contentWidth: Float, includeLocation: Boolean = false
        ): EligibleActinTrialsGenerator {
            val unavailableAndEligible = cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val title =
                "${createTableTitleStart(source)} and cohorts that are potentially eligible, but are closed (${unavailableAndEligible.size})"
            return create(unavailableAndEligible, title, contentWidth, null, includeLocation)
        }

        private fun create(
            cohorts: List<InterpretedCohort>, title: String, width: Float, footNote: String? = null, includeLocation: Boolean
        ): EligibleActinTrialsGenerator {
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val locationColWidth = if (includeLocation) width / 7 else 0f
            val checksColWidth = width - (trialColWidth + cohortColWidth + molecularColWidth + locationColWidth)
            return EligibleActinTrialsGenerator(
                cohorts,
                title,
                footNote,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                locationColWidth.takeIf { includeLocation },
                checksColWidth
            )
        }
    }
}