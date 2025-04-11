package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

private const val SMALL_PADDING_DISTANCE = 0.1f
private const val NORMAL_PADDING_DISTANCE = 1f

class IneligibleTrialGenerator(
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

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, subTableWidths.sum())
        val subTableHeaders =
            listOfNotNull("Cohort", "Molecular", "Sites", "Ineligibility reasons".takeIf { includeIneligibilityReasonCol })

        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val subTable = Tables.createFixedWidthCols(*subTableWidths)
            subTableHeaders.map(Cells::createHeader).forEach(subTable::addHeaderCell)
            table.addHeaderCell(Cells.createContentNoBorder(subTable))
        }

        addTrialsToTable(
            cohorts,
            externalTrials = emptySet(),
            requestingSource,
            countryOfReference = null,
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
        return table
    }

    override fun getCohortSize(): Int {
        return cohorts.size
    }

    companion object {
        fun forEvaluableCohorts(
            cohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            openOnly: Boolean = false
        ): IneligibleTrialGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || !openOnly) }
            val (trialColWidth, subTableWidths) = determineRelativeColumnWidths(true)
            val title =
                "Trials and cohorts that are considered ineligible (${ineligibleCohorts.size})"
            val footNote = if (!openOnly) {
                "Closed cohorts are shown in grey.".takeUnless { ineligibleCohorts.all(InterpretedCohort::isOpen) }
            } else null
            return IneligibleTrialGenerator(
                cohorts = ineligibleCohorts,
                requestingSource = requestingSource,
                title = title,
                footNote = footNote,
                trialColWidth = trialColWidth,
                subTableWidths = subTableWidths,
                includeIneligibilityReasonCol = true,
                paddingDistance = NORMAL_PADDING_DISTANCE,
                allowDeEmphasis = true
            )
        }

        fun forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?
        ): IneligibleTrialGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val (trialColWidth, subTableWidths) = determineRelativeColumnWidths(includeIneligibilityReason = false)
            val title = "Trials and cohorts that are not evaluable or ignored (${nonEvaluableAndIgnoredCohorts.size})"

            return IneligibleTrialGenerator(
                cohorts = nonEvaluableAndIgnoredCohorts,
                requestingSource = requestingSource,
                title = title,
                footNote = null,
                trialColWidth = trialColWidth,
                subTableWidths = subTableWidths,
                includeIneligibilityReasonCol = false,
                paddingDistance = SMALL_PADDING_DISTANCE,
                allowDeEmphasis = false
            )
        }

        private fun determineRelativeColumnWidths(includeIneligibilityReason: Boolean = false): Pair<Float, FloatArray> {
            val base = 1f
            val trialWidth = if (includeIneligibilityReason) base / 9 else base / 4
            val cohortWidth = if (includeIneligibilityReason) base / 4 else base / 2
            val molecularWidth = if (includeIneligibilityReason) base / 7 else base / 4
            val sitesWidth = if (includeIneligibilityReason) base / 7 else base / 4
            val remainingWidth = base - (trialWidth + cohortWidth + molecularWidth + sitesWidth)

            return trialWidth to listOfNotNull(
                cohortWidth,
                molecularWidth,
                sitesWidth,
                remainingWidth.takeIf { includeIneligibilityReason }
            ).toFloatArray()
        }
    }
}