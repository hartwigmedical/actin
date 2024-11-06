package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.addTrialsToTable
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleActinTrialsGenerator(
    private val cohorts: List<InterpretedCohort>,
    private val title: String,
    private val footNote: String?,
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

        addTrialsToTable(cohorts, table, floatArrayOf(cohortColWidth, molecularEventColWidth, checksColWidth), InterpretedCohort::warnings)

        if (footNote != null) {
            table.addCell(Cells.createSpanningSubNote(footNote, table))
        }

        return makeWrapping(table)
    }

    fun getCohortSize(): Int {
        return cohorts.size
    }

    companion object {

        fun forOpenCohorts(
            cohorts: List<InterpretedCohort>, source: String?, width: Float, slotsAvailable: Boolean
        ): Pair<EligibleActinTrialsGenerator, List<InterpretedCohort>> {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.hasSlotsAvailable == slotsAvailable && !it.isMissingGenesForSufficientEvaluation!!
            }
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
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
            cohorts: List<InterpretedCohort>, source: String?, width: Float
        ): EligibleActinTrialsGenerator? {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.isMissingGenesForSufficientEvaluation!!
            }
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(InterpretedCohort::trialId).distinct()
            val cohortFromTrialsText = if (recruitingAndEligibleCohorts.isNotEmpty()) {
                "(${formatCountWithLabel(recruitingAndEligibleCohorts.size, "cohort")}" +
                        " from ${formatCountWithLabel(recruitingAndEligibleTrials.size, "trial")})"
            } else "(0)"

            val titleStart = ActinTrialGeneratorFunctions.createTableTitleStart(source)
            val title =
                "$titleStart that are open but for which additional molecular tests needed to evaluate eligibility $cohortFromTrialsText"

            val footNote = "Open cohorts with no slots available are shown in grey."

            return if (recruitingAndEligibleCohorts.isNotEmpty()) create(recruitingAndEligibleCohorts, title, width, footNote) else null
        }

        private fun formatCountWithLabel(count: Int, word: String): String {
            return "$count $word${if (count > 1) "s" else ""}"
        }

        fun forClosedCohorts(
            cohorts: List<InterpretedCohort>,
            source: String?,
            contentWidth: Float,
        ): EligibleActinTrialsGenerator {
            val unavailableAndEligible =
                cohorts.filter { trial: InterpretedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
            val titleStart = ActinTrialGeneratorFunctions.createTableTitleStart(source)
            val title = String.format(
                "%s and cohorts that are potentially eligible, but are closed (%s)",
                titleStart,
                unavailableAndEligible.size
            )
            return create(unavailableAndEligible, title, contentWidth)
        }

        private fun create(
            cohorts: List<InterpretedCohort>,
            title: String,
            width: Float,
            footNote: String? = null
        ): EligibleActinTrialsGenerator {
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val checksColWidth = width - (trialColWidth + cohortColWidth + molecularColWidth)
            return EligibleActinTrialsGenerator(
                cohorts,
                title,
                footNote,
                trialColWidth,
                cohortColWidth,
                molecularColWidth,
                checksColWidth
            )
        }
    }
}