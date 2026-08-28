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
    private val useIneligibilityInsteadOfSite: Boolean,
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
        val locationColWidth = 40f
        val ineligibilityColWidth = 54f

        val table = if (useIneligibilityInsteadOfSite) Tables.createRelativeWidthCols(
            trialColWidth,
            cohortColWidth,
            molecularColWidth,
            ineligibilityColWidth
        ) else Tables.createRelativeWidthCols(trialColWidth, cohortColWidth, molecularColWidth, locationColWidth)

        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colTrial()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colCohort()))
        table.addHeaderCell(Cells.createHeader(labels.trialMatching.colMolecular()))
        if (useIneligibilityInsteadOfSite) {
            table.addHeaderCell(Cells.createHeader(labels.trialMatching.colIneligibilityReasons()))
        } else {
            table.addHeaderCell(Cells.createHeader(labels.trialMatching.colSites()))
        }

        addTrialsToTable(
            table = table,
            cohorts = cohorts,
            externalTrials = emptySet(),
            requestingSource = requestingSource,
            countryOfReference = null,
            includeFeedback = useIneligibilityInsteadOfSite,
            feedbackFunction = InterpretedCohort::fails,
            indicateNoSlotsOrClosed = indicateNoSlotsOrClosed,
            useSmallerSize = true,
            includeSites = !useIneligibilityInsteadOfSite
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
                useIneligibilityInsteadOfSite = true,
                labels = labels
            )
        }

        fun nonEvaluableCohorts(
            nonEvaluableCohorts: List<InterpretedCohort>,
            requestingSource: TrialSource?,
            labels: ReportLabels
        ): TrialTableGenerator {
            val nonEvaluableTrials = nonEvaluableCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortsString = TrialFormatFunctions.generateCohortsFromTrialsString(
                nonEvaluableCohorts.size,
                nonEvaluableTrials.size,
                labels
            )
            val title = labels.trialMatching.titleNonEvaluable(cohortsString)

            return IneligibleTrialGenerator(
                cohorts = nonEvaluableCohorts,
                requestingSource = requestingSource,
                title = title,
                indicateNoSlotsOrClosed = false,
                useIneligibilityInsteadOfSite = false,
                labels = labels
            )
        }
    }
}
