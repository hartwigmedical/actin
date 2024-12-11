package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.createTableTitleStart
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
    private val paddingDistance: Float,
    private val includeLocationColumn: Boolean = false
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
                if (includeLocationColumn) {
                    addHeaderCell(Cells.createHeader("Hospitals"))
                }
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
                paddingDistance,
                includeLocationColumn
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
            source: String?,
            width: Float,
            enableExtendedMode: Boolean,
            includeLocationColumn: Boolean = false
        ): IneligibleActinTrialsGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || enableExtendedMode) }
            val (trialColWidth, subTableWidths) = getColumnWidths(width, true, includeLocationColumn)
            val title =
                "${createTableTitleStart(source)} and cohorts that are ${if (enableExtendedMode) "" else "open but "}considered ineligible (${ineligibleCohorts.size})"
            enableExtendedMode.let { "open but " }
            return IneligibleActinTrialsGenerator(
                ineligibleCohorts,
                title,
                trialColWidth,
                subTableWidths,
                true,
                paddingDistance = NORMAL_PADDING_DISTANCE,
                includeLocationColumn
            )
        }

        fun forClosedCohorts(
            cohorts: List<InterpretedCohort>, source: String?, width: Float, includeLocationColumn: Boolean = false
        ): IneligibleActinTrialsGenerator {
            val unavailableAndEligible = cohorts.filter { trial: InterpretedCohort -> !trial.isPotentiallyEligible && !trial.isOpen }
            val (trialColWidth, subTableWidths) = getColumnWidths(width, false, includeLocationColumn)
            val title =
                "${createTableTitleStart(source)} and cohorts that are closed and considered ineligible (${unavailableAndEligible.size})"
            return IneligibleActinTrialsGenerator(
                unavailableAndEligible,
                title,
                trialColWidth,
                subTableWidths,
                false,
                paddingDistance = SMALL_PADDING_DISTANCE,
                includeLocationColumn
            )
        }

        fun forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            source: String?,
            width: Float,
            includeLocationColumn: Boolean = false
        ): IneligibleActinTrialsGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val (trialColWidth, subTableWidths) = getColumnWidths(width, false, includeLocationColumn)
            val title =
                "${createTableTitleStart(source)} and cohorts that are not evaluable or ignored (${nonEvaluableAndIgnoredCohorts.size})"
            return IneligibleActinTrialsGenerator(
                nonEvaluableAndIgnoredCohorts,
                title,
                trialColWidth,
                subTableWidths,
                false,
                paddingDistance = SMALL_PADDING_DISTANCE,
                includeLocationColumn
            )
        }

        private fun getColumnWidths(
            width: Float, includeIneligibilityReason: Boolean = false, includeLocation: Boolean = false
        ): Pair<Float, FloatArray> = width.let { w ->
            val trialWidth = if (includeIneligibilityReason) w / 9 else w / 4
            val cohortWidth = if (includeIneligibilityReason) w / 4 else w / 2
            val molecularWidth = if (includeIneligibilityReason) w / 7 else w / 4
            val hospitalsWidth = when {
                includeLocation && includeIneligibilityReason -> w / 7
                includeLocation -> w / 4
                else -> 0f
            }
            val remainingWidth = w - (trialWidth + cohortWidth + molecularWidth + hospitalsWidth)
            trialWidth to listOfNotNull(
                cohortWidth,
                molecularWidth,
                hospitalsWidth.takeIf { includeLocation },
                remainingWidth.takeIf { includeIneligibilityReason }).toFloatArray()
        }
    }
}