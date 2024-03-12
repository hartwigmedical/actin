package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class IneligibleActinTrialsGenerator private constructor(
    private val cohorts: List<EvaluatedCohort>,
    private val source: String,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val ineligibilityReasonColWith: Float,
    private val enableExtendedMode: Boolean
) : TableGenerator {

    override fun title(): String {
        return String.format(
            "%s trials and cohorts that are %sconsidered ineligible (%s)",
            source,
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
        ActinTrialGeneratorFunctions.sortedCohortGroups(cohorts).forEach { cohortList: List<EvaluatedCohort> ->
            val trialSubTable = Tables.createFixedWidthCols(
                cohortColWidth, molecularEventColWidth, ineligibilityReasonColWith
            )
            cohortList.forEach { cohort: EvaluatedCohort ->
                val cohortText = ActinTrialGeneratorFunctions.createCohortString(cohort)
                val molecularText = cohort.molecularEvents.sorted().joinToString(", ")
                val ineligibilityText = if (cohort.fails.isEmpty()) "?" else cohort.fails.sorted().joinToString(", ")
                ActinTrialGeneratorFunctions.addContentListToTable(
                    listOf(cohortText, molecularText, ineligibilityText),
                    !cohort.isOpen || !cohort.hasSlotsAvailable,
                    trialSubTable
                )
            }
            ActinTrialGeneratorFunctions.insertTrialRow(cohortList, table, trialSubTable)
        }
        val subNote = listOfNotNull(
            if (cohorts.any { !it.isOpen }) " Cohorts shown in grey are closed or have no slots available." else null,
            if (cohorts.any { it.isOpen && !it.hasSlotsAvailable }) {
                " Open cohorts with no slots available are indicated by an asterisk (*)."
            } else null
        ).joinToString("")

        if (subNote.isNotEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table))
        }
        return makeWrapping(table)
    }

    companion object {
        fun fromEvaluatedCohorts(
            cohorts: List<EvaluatedCohort>, source: String, contentWidth: Float, enableExtendedMode: Boolean
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