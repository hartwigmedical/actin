package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleActinTrialsGenerator private constructor(
    private val cohorts: List<EvaluatedCohort>,
    private val title: String,
    private val trialColWidth: Float,
    private val cohortColWidth: Float,
    private val molecularEventColWidth: Float,
    private val checksColWidth: Float
) : TableGenerator {
    override fun title(): String {
        return title
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + molecularEventColWidth + checksColWidth)

        if (cohorts.isNotEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = Tables.createFixedWidthCols(
                cohortColWidth, molecularEventColWidth, checksColWidth
            )
            sequenceOf("Cohort", "Molecular", "Warnings").map(Cells::createHeader).forEach(headerSubTable::addHeaderCell)
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }

        addTrialsToTable(cohorts, table, cohortColWidth, molecularEventColWidth, checksColWidth, EvaluatedCohort::warnings)
        return makeWrapping(table)
    }

    companion object {

        fun forOpenCohorts(
            cohorts: List<EvaluatedCohort>, source: String?, width: Float, slotsAvailable: Boolean
        ): Pair<EligibleActinTrialsGenerator, List<EvaluatedCohort>> {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.hasSlotsAvailable == slotsAvailable && !it.isMissingGenesForSufficientEvaluation
            }
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(EvaluatedCohort::trialId).distinct()
            val slotsText = if (!slotsAvailable) " but currently have no slots available" else ""
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size, "cohort")}" +
                        " from ${formatCountWithLabel(recruitingAndEligibleTrials.size, "trial")})"
            } else "(0)"

            val titleStart = ActinTrialGeneratorFunctions.createTableTitleStart(source)
            val title = "$titleStart that are open and potentially eligible$slotsText $cohortFromTrialsText"

            return create(recruitingAndEligibleCohorts, title, width) to recruitingAndEligibleCohorts
        }

        fun forOpenCohortsWithMissingGenes(
            cohorts: List<EvaluatedCohort>, source: String?, width: Float
        ): EligibleActinTrialsGenerator? {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.isMissingGenesForSufficientEvaluation
            }
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(EvaluatedCohort::trialId).distinct()
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size, "cohort")}" +
                        " from ${formatCountWithLabel(recruitingAndEligibleTrials.size, "trial")})"
            } else "(0)"

            val titleStart = ActinTrialGeneratorFunctions.createTableTitleStart(source)
            val title =
                "$titleStart that are open but for which additional genes need to be tested to evaluate eligibility $cohortFromTrialsText"

            return if (recruitingAndEligibleCohorts.isNotEmpty()) create(recruitingAndEligibleCohorts, title, width) else null
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }

        fun forClosedCohorts(
            cohorts: List<EvaluatedCohort>,
            source: String?,
            contentWidth: Float,
            enableExtendedMode: Boolean
        ): EligibleActinTrialsGenerator {
            val unavailableAndEligible = cohorts
                .filter { trial: EvaluatedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
                .filter { trial: EvaluatedCohort -> trial.molecularEvents.isNotEmpty() || enableExtendedMode }

            val titleStart = ActinTrialGeneratorFunctions.createTableTitleStart(source)
            val title = String.format(
                "%s and cohorts that %smay be eligible, but are closed (%s)",
                titleStart,
                if (enableExtendedMode) "" else "meet molecular requirements and ",
                unavailableAndEligible.size
            )
            return create(unavailableAndEligible, title, contentWidth)
        }

        private fun create(
            cohorts: List<EvaluatedCohort>,
            title: String,
            width: Float
        ): EligibleActinTrialsGenerator {
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val checksColWidth = width - (trialColWidth + cohortColWidth + molecularColWidth)
            return EligibleActinTrialsGenerator(
                cohorts,
                title,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                checksColWidth
            )
        }
    }
}