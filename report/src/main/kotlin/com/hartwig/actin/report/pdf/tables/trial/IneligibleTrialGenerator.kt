package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class IneligibleTrialGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val requestingSource: TrialSource?,
    private val title: String,
    private val footNote: String?,
    private val allowDeEmphasis: Boolean,
    private val includeIneligibilityColumn: Boolean,
    private val includeSitesAndCohortConfig: Boolean
) : TrialTableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val trialColWidth = 10f
        val cohortColWidth = 20f
        val molecularColWidth = 6f
        val locationColWidth = 10f
        val ineligibilityColWidth = 54f
        val configColWidth = 30f

        val table = when {
            includeIneligibilityColumn && !includeSitesAndCohortConfig ->
                Tables.createRelativeWidthCols(trialColWidth, cohortColWidth, molecularColWidth, ineligibilityColWidth)
            includeSitesAndCohortConfig && !includeIneligibilityColumn ->
                Tables.createRelativeWidthCols(trialColWidth, cohortColWidth, molecularColWidth, locationColWidth, configColWidth)
            else ->
                throw IllegalStateException("includeIneligibilityColumn and includeSitesAndCohortConfig cannot both be true")
        }

        table.addHeaderCell(Cells.createHeader("Trial"))
        table.addHeaderCell(Cells.createHeader("Cohort"))
        table.addHeaderCell(Cells.createHeader("Molecular"))
        if (includeSitesAndCohortConfig) {
            table.addHeaderCell(Cells.createHeader("Sites"))
        }
        if (includeIneligibilityColumn) {
            table.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
        }
        if (includeSitesAndCohortConfig) {
            table.addHeaderCell(Cells.createHeader("Configuration"))
        }
        
        addTrialsToTable(
            table = table,
            cohorts = cohorts,
            externalTrials = emptySet(),
            requestingSource = requestingSource,
            countryOfReference = null,
            includeFeedback = includeIneligibilityColumn,
            feedbackFunction = InterpretedCohort::fails,
            allowDeEmphasis = allowDeEmphasis,
            useSmallerSize = true,
            includeCohortConfig = includeSitesAndCohortConfig,
            includeSites = includeSitesAndCohortConfig
        )
        if (footNote != null) {
            table.addCell(Cells.createSpanningSubNote(footNote, table).setFontSize(Styles.SMALL_FONT_SIZE))
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
        ): TrialTableGenerator {
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
                allowDeEmphasis = true,
                includeIneligibilityColumn = true,
                includeSitesAndCohortConfig = false
            )
        }

        fun forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?
        ): TrialTableGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val title = "Trials and cohorts that are not evaluable or ignored (${nonEvaluableAndIgnoredCohorts.size})"

            return IneligibleTrialGenerator(
                cohorts = nonEvaluableAndIgnoredCohorts,
                requestingSource = requestingSource,
                title = title,
                footNote = null,
                allowDeEmphasis = false,
                includeIneligibilityColumn = false,
                includeSitesAndCohortConfig = true
            )
        }
    }
}