package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class IneligibleTrialGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val requestingSource: TrialSource?,
    private val title: String,
    private val footNote: String?,
    private val includeIneligibilityColumn: Boolean,
    private val allowDeEmphasis: Boolean
) : TrialTableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val trialColWidth = 1f
        val cohortColWidth = 2f
        val molecularColWidth = 1f
        val locationColWidth = 1f
        val ineligibilityColWidth = 3f

        val table =
            if (includeIneligibilityColumn) {
                Tables.createRelativeWidthCols(trialColWidth, cohortColWidth, molecularColWidth, locationColWidth, ineligibilityColWidth)
            } else {
                Tables.createRelativeWidthCols(trialColWidth, cohortColWidth, molecularColWidth, locationColWidth)
            }

        table.addHeaderCell(Cells.createHeader("Trial"))
        table.addHeaderCell(Cells.createHeader("Cohort"))
        table.addHeaderCell(Cells.createHeader("Molecular"))
        table.addHeaderCell(Cells.createHeader("Sites"))
        if (includeIneligibilityColumn) {
            table.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
        }
        
        addTrialsToTable(
            table = table,
            cohorts = cohorts,
            externalTrials = emptySet(),
            requestingSource = requestingSource,
            countryOfReference = null,
            includeFeedback = includeIneligibilityColumn,
            feedbackFunction = InterpretedCohort::fails,
            allowDeEmphasis = allowDeEmphasis
        )
        if (footNote != null) {
            table.addCell(Cells.createSpanningSubNote(footNote, table))
        }
        return table
    }

    override fun cohortSize(): Int {
        return cohorts.size
    }

    companion object {
        
        fun forEvaluableCohorts(
            cohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            openOnly: Boolean = false
        ): IneligibleTrialGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || !openOnly) }
            val title = "Trials and cohorts that are considered ineligible (${ineligibleCohorts.size})"
            val footNote = if (!openOnly) {
                "Closed cohorts are shown in grey.".takeUnless { ineligibleCohorts.all(InterpretedCohort::isOpen) }
            } else null
            
            return IneligibleTrialGenerator(
                cohorts = ineligibleCohorts,
                requestingSource = requestingSource,
                title = title,
                footNote = footNote,
                includeIneligibilityColumn = true,
                allowDeEmphasis = true
            )
        }

        fun forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?
        ): IneligibleTrialGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val title = "Trials and cohorts that are not evaluable or ignored (${nonEvaluableAndIgnoredCohorts.size})"

            return IneligibleTrialGenerator(
                cohorts = nonEvaluableAndIgnoredCohorts,
                requestingSource = requestingSource,
                title = title,
                footNote = null,
                includeIneligibilityColumn = false,
                allowDeEmphasis = false
            )
        }
    }
}