package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.Cohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class IneligibleActinTrialsGenerator(
    private val cohorts: List<Cohort>,
    private val title: String,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val ineligibilityReasonColWith: Float,
    private val includeIneligibilityReasonCol: Boolean
) : TableGenerator {

    override fun title(): String {
        return title
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + molecularEventColWidth + ineligibilityReasonColWith)
        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = Tables.createFixedWidthCols(
                cohortColWidth, molecularEventColWidth, ineligibilityReasonColWith
            )
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"))
            headerSubTable.addHeaderCell(Cells.createHeader("Molecular"))
            if (includeIneligibilityReasonCol) {
                headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
            }
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        val feedbackFunction = if (includeIneligibilityReasonCol) Cohort::fails else { _: Cohort -> emptySet() }
        addTrialsToTable(cohorts, table, cohortColWidth, molecularEventColWidth, ineligibilityReasonColWith, feedbackFunction)
        val subNote = "Open cohorts with no slots available are shown in grey."
        if (includeIneligibilityReasonCol && cohorts.isNotEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table))
        }
        return makeWrapping(table)
    }

    companion object {
        fun forOpenCohorts(
            cohorts: List<Cohort>, source: String, width: Float, enableExtendedMode: Boolean
        ): IneligibleActinTrialsGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || enableExtendedMode) }
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val ineligibilityReasonColWidth = width - (trialColWidth + cohortColWidth + molecularColWidth)
            val title = String.format(
                "%s trials and cohorts that are %sconsidered ineligible (%s)",
                source,
                if (enableExtendedMode) "" else "open but ",
                ineligibleCohorts.size
            )
            return create(ineligibleCohorts, title, trialColWidth, cohortColWidth, molecularColWidth, ineligibilityReasonColWidth, true)
        }

        fun forClosedCohorts(
            cohorts: List<Cohort>,
            source: String,
            width: Float,
        ): IneligibleActinTrialsGenerator {
            val unavailableAndEligible =
                cohorts.filter { trial: Cohort -> !trial.isPotentiallyEligible && !trial.isOpen }
            val title = String.format(
                "%s trials and cohorts that are closed and considered ineligible (%s)",
                source,
                unavailableAndEligible.size
            )
            return create(unavailableAndEligible, title, width, width * 2, width)
        }

        fun forNonEvaluableCohorts(
            cohorts: List<Cohort>,
            source: String,
            width: Float,
        ): IneligibleActinTrialsGenerator {
            val title = String.format("%s trials and cohorts that are not evaluable or ignored (%s)", source, cohorts.size)
            return create(cohorts, title, width, width * 2, width)
        }

        private fun create(
            cohorts: List<Cohort>,
            title: String,
            trialColWidth: Float,
            cohortColWidth: Float,
            molecularColWidth: Float,
            ineligibilityReasonColWidth: Float = 0f,
            includeIneligibilityReasonCol: Boolean = false
        ): IneligibleActinTrialsGenerator {
            return IneligibleActinTrialsGenerator(
                cohorts,
                title,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth,
                includeIneligibilityReasonCol
            )
        }
    }
}