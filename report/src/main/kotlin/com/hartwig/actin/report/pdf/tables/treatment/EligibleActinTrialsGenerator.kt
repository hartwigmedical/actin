package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.treatment.ActinTrialGeneratorFunctions.addTrialsToTable
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
            listOf("Cohort", "Molecular", "Warnings").forEach { headerSubTable.addHeaderCell(Cells.createHeader(it)) }
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }

        addTrialsToTable(cohorts, table, cohortColWidth, molecularEventColWidth, checksColWidth, EvaluatedCohort::warnings)
        return makeWrapping(table)
    }

    companion object {

        fun forOpenCohorts(
            cohorts: List<EvaluatedCohort>, source: String, width: Float, slotsAvailable: Boolean
        ): EligibleActinTrialsGenerator {
            val recruitingAndEligibleCohorts = cohorts.filter {
                it.isPotentiallyEligible && it.isOpen &&
                        it.hasSlotsAvailable == slotsAvailable
            }
            val recruitingAndEligibleTrials = recruitingAndEligibleCohorts.map(EvaluatedCohort::trialId).distinct()
            val title = "$source trials that are open and considered eligible" +
                    if (slotsAvailable) "and currently have slots available " else "but currently have no slots available" +
                            "(${recruitingAndEligibleCohorts.size} cohorts from ${recruitingAndEligibleTrials.size} trials)"
            return create(recruitingAndEligibleCohorts, title, width)
        }

        fun forClosedCohorts(
            cohorts: List<EvaluatedCohort>,
            source: String,
            contentWidth: Float,
            enableExtendedMode: Boolean
        ): EligibleActinTrialsGenerator {
            val unavailableAndEligible = cohorts
                .filter { trial: EvaluatedCohort -> trial.isPotentiallyEligible && !trial.isOpen }
                .filter { trial: EvaluatedCohort -> trial.molecularEvents.isNotEmpty() || enableExtendedMode }

            val title = String.format(
                "%s trials and cohorts that %smay be eligible, but are closed (%s)",
                source,
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