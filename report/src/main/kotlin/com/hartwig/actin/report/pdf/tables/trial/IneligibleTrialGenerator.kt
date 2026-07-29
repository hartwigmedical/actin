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

        table.addHeaderCell(Cells.createHeader(labels.trialColTrial()))
        table.addHeaderCell(Cells.createHeader(labels.trialColCohort()))
        table.addHeaderCell(Cells.createHeader(labels.trialColMolecular()))
        if (!useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader(labels.trialColSites()))
        }
        if (useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader(labels.trialColIneligibilityReasons()))
        }
        if (!useIneligibilityInsteadOfSiteAndConfig) {
            table.addHeaderCell(Cells.createHeader(labels.trialColConfiguration()))
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
            val title = labels.trialTitleIneligible(cohortsString)

            return IneligibleTrialGenerator(
                cohorts = ineligibleCohorts,
                requestingSource = requestingSource,
                title = title,
                indicateNoSlotsOrClosed = true,
                useIneligibilityInsteadOfSiteAndConfig = true,
                labels = labels
            )
        }

        fun nonEvaluableOrIgnoredCohorts(
            ignoredCohorts: List<InterpretedCohort>,
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            labels: ReportLabels
        ): TrialTableGenerator {
            val nonEvaluableAndIgnoredCohorts = ignoredCohorts + nonEvaluableCohorts
            val nonEvaluableAndIgnoredTrials = nonEvaluableAndIgnoredCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                nonEvaluableAndIgnoredCohorts.size,
                nonEvaluableAndIgnoredTrials.size,
                labels
            )
            val title = labels.trialTitleNonEvaluable(cohortsString)

            return IneligibleTrialGenerator(
                cohorts = nonEvaluableAndIgnoredCohorts,
                requestingSource = requestingSource,
                title = title,
                indicateNoSlotsOrClosed = false,
                useIneligibilityInsteadOfSiteAndConfig = false,
                labels = labels
            )
        }
    }
}
