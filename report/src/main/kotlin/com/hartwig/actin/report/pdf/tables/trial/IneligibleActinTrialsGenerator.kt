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
    private val subTableWidths: FloatArray,
    private val includeIneligibilityReasonCol: Boolean,
    private val paddingDistance: Float
) : TableGenerator {

    override fun title(): String {
        return title
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, subTableWidths.sum())
        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = Tables.createFixedWidthCols(*subTableWidths).apply {
                addHeaderCell(Cells.createHeader("Cohort"))
                addHeaderCell(Cells.createHeader("Molecular"))
                if (includeIneligibilityReasonCol) {
                    addHeaderCell(Cells.createHeader("Ineligibility reasons"))
                }
            }
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))

            addTrialsToTable(
                cohorts,
                table,
                subTableWidths,
                InterpretedCohort::fails,
                includeIneligibilityReasonCol,
                paddingDistance
            )

            if (includeIneligibilityReasonCol) {
                table.addCell(Cells.createSpanningSubNote("Open cohorts with no slots available are shown in grey.", table))
            }
        }
        return makeWrapping(table)
    }

    fun getCohortSize(): Int {
        return cohorts.size
    }

    companion object {
        fun forOpenCohorts(
            cohorts: List<InterpretedCohort>,
            source: String,
            width: Float,
            enableExtendedMode: Boolean
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
            return IneligibleActinTrialsGenerator(
                ineligibleCohorts,
                title,
                trialColWidth,
                floatArrayOf(cohortColWidth, molecularColWidth, ineligibilityReasonColWidth),
                true,
                paddingDistance = NORMAL_PADDING_DISTANCE
            )
        }

        fun forClosedCohorts(
            cohorts: List<InterpretedCohort>,
            source: String,
            width: Float,
        ): IneligibleActinTrialsGenerator {
            val unavailableAndEligible =
                cohorts.filter { trial: InterpretedCohort -> !trial.isPotentiallyEligible && !trial.isOpen }
            val title = String.format(
                "%s trials and cohorts that are closed and considered ineligible (%s)",
                source,
                unavailableAndEligible.size
            )
            return IneligibleActinTrialsGenerator(
                unavailableAndEligible,
                title,
                width / 4,
                floatArrayOf(width / 2, width / 4),
                false,
                paddingDistance = SMALL_PADDING_DISTANCE
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
            return IneligibleActinTrialsGenerator(
                nonEvaluableAndIgnoredCohorts,
                title,
                width / 4,
                floatArrayOf(width / 2, width / 4),
                false,
                paddingDistance = SMALL_PADDING_DISTANCE
            )
        }
    }
}