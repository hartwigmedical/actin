package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class IneligibleActinTrialsGenerator(
    private val cohorts: List<EvaluatedCohort>,
    private val source: String?,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val ineligibilityReasonColWith: Float,
    private val enableExtendedMode: Boolean
) : TableGenerator {

    override fun title(): String {
        return String.format(
            "%s and cohorts that are %sconsidered ineligible (%s)",
            ActinTrialGeneratorFunctions.createTableTitleStart(source),
            if (enableExtendedMode) "" else "open but ",
            cohorts.size
        )
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
            headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        ActinTrialGeneratorFunctions.addTrialsToTable(
            cohorts,
            table,
            cohortColWidth,
            molecularEventColWidth,
            ineligibilityReasonColWith,
            EvaluatedCohort::fails
        )
        val subNote = "Open cohorts with no slots available are shown in grey."

        if (subNote.isNotEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table))
        }
        return makeWrapping(table)
    }

    companion object {
        fun fromEvaluatedCohorts(
            cohorts: List<EvaluatedCohort>, source: String?, contentWidth: Float, enableExtendedMode: Boolean
        ): IneligibleActinTrialsGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || enableExtendedMode) }
            val trialColWidth = contentWidth / 9
            val cohortColWidth = contentWidth / 4
            val molecularColWidth = contentWidth / 7
            val ineligibilityReasonColWidth = contentWidth - (trialColWidth + cohortColWidth + molecularColWidth)
            return IneligibleActinTrialsGenerator(
                ineligibleCohorts,
                source,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth,
                enableExtendedMode
            )
        }
    }
}