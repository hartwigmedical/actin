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
    private val indicateNoSlotsOrClosed: Boolean,
    private val useIneligibilityInsteadOfSiteAndConfig: Boolean
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

        val table = if (useIneligibilityInsteadOfSiteAndConfig) Tables.createRelativeWidthCols(
            trialColWidth,
            cohortColWidth,
            molecularColWidth,
            ineligibilityColWidth
        ) else Tables.createRelativeWidthCols(trialColWidth, cohortColWidth, molecularColWidth, locationColWidth, configColWidth)

        table.addHeaderCell(Cells.createHeader("Trial"))
        table.addHeaderCell(Cells.createHeader("Cohort"))
        table.addHeaderCell(Cells.createHeader("Molecular"))
        if (!useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader("Sites"))
        }
        if (useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
        }
        if (!useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader("Configuration"))
        }

        addTrialsToTable(
            table = table,
            cohorts = cohorts,
            externalTrials = emptySet(),
            requestingSource = requestingSource,
            countryOfReference = null,
            includeFeedback = useIneligibilityInsteadOfSiteAndConfig,
            feedbackFunction = InterpretedCohort::fails,
            indicateNoSlotsOrClosed = indicateNoSlotsOrClosed,
            useSmallerSize = true,
            includeCohortConfig = !useIneligibilityInsteadOfSiteAndConfig,
            includeSites = !useIneligibilityInsteadOfSiteAndConfig
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

            return IneligibleTrialGenerator(
                cohorts = ineligibleCohorts,
                requestingSource = requestingSource,
                title = title,
                footNote = null,
                indicateNoSlotsOrClosed = true,
                useIneligibilityInsteadOfSiteAndConfig = true
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
                indicateNoSlotsOrClosed = false,
                useIneligibilityInsteadOfSiteAndConfig = false
            )
        }
    }
}