package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class IneligibleActinTrialsGenerator private constructor(
    private val cohorts: List<EvaluatedCohort>,
    private val title: String,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val ineligibilityReasonColWith: Float,
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
            if (ineligibilityReasonColWith != 0f) {
                headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
            }
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        val feedbackFunction = if (ineligibilityReasonColWith != 0f) EvaluatedCohort::fails else { _: EvaluatedCohort -> emptySet() }
        addTrialsToTable(cohorts, table, cohortColWidth, molecularEventColWidth, ineligibilityReasonColWith, feedbackFunction)
        val subNote = "Open cohorts with no slots available are shown in grey."
        if (ineligibilityReasonColWith != 0f) {
            table.addCell(Cells.createSpanningSubNote(subNote, table))
        }
        return makeWrapping(table)
    }

    companion object {
        fun forOpenCohorts(
            cohorts: List<EvaluatedCohort>, source: String, width: Float, enableExtendedMode: Boolean
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
                cohorts.size
            )
            return IneligibleActinTrialsGenerator(
                ineligibleCohorts,
                title,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth
            )
        }

        fun forClosedCohorts(
            cohorts: List<EvaluatedCohort>,
            source: String,
            width: Float,
        ): IneligibleActinTrialsGenerator {
            val unavailableAndEligible = cohorts.filter { trial: EvaluatedCohort -> !trial.isPotentiallyEligible && !trial.isOpen }
            val trialColWidth = width / 3
            val cohortColWidth = width * 2 / 3
            val molecularColWidth = width / 3
            val ineligibilityReasonColWidth = 0f
            val title = String.format(
                "%s trials and cohorts that are considered ineligible, and are closed (%s)",
                source,
                unavailableAndEligible.size
            )
            return IneligibleActinTrialsGenerator(
                unavailableAndEligible,
                title,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth
            )
        }
    }
}