package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class IneligibleTrialGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val requestingSource: TrialSource?,
    private val title: String,
    private val indicateNoSlotsOrClosed: Boolean,
    private val useIneligibilityInsteadOfSiteAndConfig: Boolean,
    private val labels: ReportLabels
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

        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colTrial()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colCohort()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colMolecular()))
        if (!useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader(labels.trialMatching.colSites()))
        }
        if (useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader(labels.trialMatching.colIneligibilityReasons()))
        }
        if (!useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader(labels.trialMatching.colConfiguration()))
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
        return table
    }

    override fun cohortSize(): Int {
        return cohorts.size
    }

    companion object {

        fun evaluableCohorts(
            cohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            labels: ReportLabels,
            openOnly: Boolean = false
        ): TrialTableGenerator {
            val ineligibleCohorts = cohorts.filter { !it.isPotentiallyEligible && (it.isOpen || !openOnly) }
            val ineligibleTrials = ineligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                ineligibleCohorts.size,
                ineligibleTrials.size,
                labels
            )
            val title = labels.trialMatching.titleIneligible(cohortsString)

            return IneligibleTrialGenerator(
                cohorts = ineligibleCohorts,
                requestingSource = requestingSource,
                title = title,
                indicateNoSlotsOrClosed = true,
                useIneligibilityInsteadOfSiteAndConfig = true,
                labels = labels
            )
        }

        fun nonEvaluableCohorts(
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            labels: ReportLabels
        ): TrialTableGenerator {
            val nonEvaluableAndIgnoredTrials = nonEvaluableCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                nonEvaluableCohorts.size,
                nonEvaluableAndIgnoredTrials.size,
                labels
            )
            val title = labels.trialMatching.titleNonEvaluable(cohortsString)

            return IneligibleTrialGenerator(
                cohorts = nonEvaluableCohorts,
                requestingSource = requestingSource,
                title = title,
                indicateNoSlotsOrClosed = false,
                useIneligibilityInsteadOfSiteAndConfig = false,
                labels = labels
            )
        }
    }
}
