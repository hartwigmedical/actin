package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

private const val SMALL_PADDING_DISTANCE = 0.1f
private const val NORMAL_PADDING_DISTANCE = 1f

class IneligibleActinTrialsGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val title: String,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val ineligibilityReasonColWith: Float,
    private val includeIneligibilityReasons: Boolean,
    private val paddingDistance: Float
) : TableGenerator {

    override fun title(): String {
        return title
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + molecularEventColWidth + ineligibilityReasonColWith)
        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = if (includeIneligibilityReasons) {
                Tables.createFixedWidthCols(cohortColWidth, molecularEventColWidth, ineligibilityReasonColWith).apply {
                    addHeaderCell(Cells.createHeader("Cohort"))
                    addHeaderCell(Cells.createHeader("Molecular"))
                    addHeaderCell(Cells.createHeader("Ineligibility reasons"))
                }
            } else {
                Tables.createFixedWidthCols(cohortColWidth, molecularEventColWidth).apply {
                    addHeaderCell(Cells.createHeader("Cohort"))
                    addHeaderCell(Cells.createHeader("Molecular"))
                }
            }
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        addTrialsToTable(
            cohorts,
            table,
            cohortColWidth,
            molecularEventColWidth,
            ineligibilityReasonColWith,
            InterpretedCohort::fails,
            includeIneligibilityReasons,
            paddingDistance
        )
        if (includeIneligibilityReasons && cohorts.isNotEmpty()) {
            table.addCell(Cells.createSpanningSubNote("Open cohorts with no slots available are shown in grey.", table))
        }
        return makeWrapping(table)
    }

    companion object {
        fun forEvaluableCohorts(
            cohorts: List<InterpretedCohort>, source: String, width: Float, includeOpen: Boolean, includeClosed: Boolean
        ): IneligibleActinTrialsGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen == includeOpen || (includeClosed && includeOpen)) }
            val trialColWidth =  if (includeOpen) width / 9 else width / 4
            val cohortColWidth = if (includeOpen) width / 4 else width / 2
            val molecularColWidth = if (includeOpen) width / 7 else width / 4
            val ineligibilityReasonColWidth = if (includeOpen) {width - (trialColWidth + cohortColWidth + molecularColWidth)} else 0f
            val title = String.format(
                "%s trials and cohorts that are %sconsidered ineligible (%s)",
                source,
                if (includeOpen) {
                    if (includeClosed) "" else "open but "
                } else {
                    "closed and "
                },
                ineligibleCohorts.size
            )
            val paddingDistance = if (!includeOpen && includeClosed) SMALL_PADDING_DISTANCE else NORMAL_PADDING_DISTANCE
            return create(
                ineligibleCohorts,
                title,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth,
                includeIneligibilityReasons = includeOpen,
                paddingDistance = paddingDistance
            )
        }

        fun forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            source: String,
            width: Float,
        ): IneligibleActinTrialsGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val title =
                String.format("%s trials and cohorts that are not evaluable or ignored (%s)", source, nonEvaluableAndIgnoredCohorts.size)
            return create(nonEvaluableAndIgnoredCohorts, title, width / 4, width / 2, width / 4, paddingDistance = SMALL_PADDING_DISTANCE)
        }

        private fun create(
            cohorts: List<InterpretedCohort>,
            title: String,
            trialColWidth: Float,
            cohortColWidth: Float,
            molecularColWidth: Float,
            ineligibilityReasonColWidth: Float = 0f,
            includeIneligibilityReasons: Boolean = false,
            paddingDistance: Float,
        ): IneligibleActinTrialsGenerator {
            return IneligibleActinTrialsGenerator(
                cohorts,
                title,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth,
                includeIneligibilityReasons,
                paddingDistance
            )
        }
    }
}