package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

private const val SMALL_PADDING_DISTANCE = 0.1f
private const val NORMAL_PADDING_DISTANCE = 1f

class IneligibleTrialTableGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val requestingSource: TrialSource?,
    private val title: String,
    private val footNote: String?,
    private val trialColWidth: Float,
    private val subTableWidths: FloatArray,
    private val includeIneligibilityReasonCol: Boolean,
    private val paddingDistance: Float,
    private val allowDeEmphasis: Boolean
) : TrialTableGenerator {

    override fun title(): String {
        return title
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, subTableWidths.sum())
        val headers = sequenceOf("Trial", "Cohort", "Molecular", "Sites", "Ineligibility reasons".takeIf { includeIneligibilityReasonCol })
            .filterNotNull()
        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader(headers.first())))
            val headerSubTable = Tables.createFixedWidthCols(*subTableWidths)
            headers.drop(1).map(Cells::createHeader).forEach(headerSubTable::addHeaderCell)
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        addTrialsToTable(
            cohorts,
            externalTrials = emptySet(),
            requestingSource,
            homeCountry = null,
            table,
            subTableWidths,
            InterpretedCohort::fails,
            includeIneligibilityReasonCol,
            paddingDistance,
            allowDeEmphasis
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
        fun forEvaluableCohorts(
            cohorts: List<InterpretedCohort>, requestingSource: TrialSource?, width: Float, openOnly: Boolean = false
        ): IneligibleTrialTableGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || !openOnly) }
            val (trialColWidth, subTableWidths) = getColumnWidths(width, true)
            val title =
                "Trials and cohorts that are considered ineligible (${ineligibleCohorts.size})"
            val footNote = if (!openOnly) "Closed cohorts are shown in grey." else null
            return IneligibleTrialTableGenerator(
                ineligibleCohorts,
                requestingSource,
                title,
                footNote,
                trialColWidth,
                subTableWidths,
                true,
                paddingDistance = NORMAL_PADDING_DISTANCE,
                true
            )
        }

        fun forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            width: Float,
        ): IneligibleTrialTableGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val (trialColWidth, subTableWidths) = getColumnWidths(width, false)
            val title =
                "Trials and cohorts that are not evaluable or ignored (${nonEvaluableAndIgnoredCohorts.size})"
            return IneligibleTrialTableGenerator(
                nonEvaluableAndIgnoredCohorts,
                requestingSource,
                title,
                footNote = null,
                trialColWidth,
                subTableWidths,
                false,
                paddingDistance = SMALL_PADDING_DISTANCE,
                false
            )
        }

        private fun getColumnWidths(
            width: Float, includeIneligibilityReason: Boolean = false
        ): Pair<Float, FloatArray> = width.let { w ->
            val trialWidth = if (includeIneligibilityReason) w / 9 else w / 4
            val cohortWidth = if (includeIneligibilityReason) w / 4 else w / 2
            val molecularWidth = if (includeIneligibilityReason) w / 7 else w / 4
            val sitesWidth = if (includeIneligibilityReason) w / 7 else w / 4
            val remainingWidth = w - (trialWidth + cohortWidth + molecularWidth + sitesWidth)
            trialWidth to listOfNotNull(
                cohortWidth,
                molecularWidth,
                sitesWidth,
                remainingWidth.takeIf { includeIneligibilityReason }
            ).toFloatArray()
        }
    }
}