package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.TreatmentConstants
import com.itextpdf.layout.element.Table
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

class EligibleActinTrialsGenerator private constructor(
    private val cohorts: List<EvaluatedCohort?>,
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
        if (!cohorts.isEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = Tables.createFixedWidthCols(
                cohortColWidth, molecularEventColWidth, checksColWidth
            )
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"))
            headerSubTable.addHeaderCell(Cells.createHeader("Molecular"))
            headerSubTable.addHeaderCell(Cells.createHeader("Warnings"))
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        ActinTrialGeneratorFunctions.streamSortedCohorts(cohorts).forEach { cohortList: List<EvaluatedCohort?>? ->
            val trialSubTable = Tables.createFixedWidthCols(
                cohortColWidth, molecularEventColWidth, checksColWidth
            )
            cohortList!!.forEach(Consumer { cohort: EvaluatedCohort? ->
                val cohortText = ActinTrialGeneratorFunctions.createCohortString(cohort)
                val cellContents = Stream.of(
                    cohortText, concat(
                        cohort!!.molecularEvents()
                    ), concat(cohort.warnings())
                )
                ActinTrialGeneratorFunctions.addContentStreamToTable(
                    cellContents,
                    !cohort.isOpen || !cohort.hasSlotsAvailable(),
                    trialSubTable
                )
            })
            ActinTrialGeneratorFunctions.insertTrialRow(cohortList, table, trialSubTable)
        }
        return makeWrapping(table)
    }

    companion object {
        fun forOpenCohortsWithSlots(cohorts: List<EvaluatedCohort?>, width: Float): EligibleActinTrialsGenerator {
            val recruitingAndEligible = cohorts.stream()
                .filter { cohort: EvaluatedCohort? -> cohort!!.isPotentiallyEligible && cohort.isOpen && cohort.hasSlotsAvailable() }
                .collect(Collectors.toList())
            val title = String.format(
                "%s trials that are open and considered eligible and currently have slots available (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                recruitingAndEligible.size
            )
            return create(recruitingAndEligible, title, width)
        }

        fun forOpenCohortsWithNoSlots(cohorts: List<EvaluatedCohort?>, width: Float): EligibleActinTrialsGenerator {
            val recruitingAndEligibleWithNoSlots = cohorts.stream()
                .filter { cohort: EvaluatedCohort? -> cohort!!.isPotentiallyEligible && cohort.isOpen && !cohort.hasSlotsAvailable() }
                .collect(Collectors.toList())
            val title = String.format(
                "%s trials that are open and considered eligible but currently have no slots available (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                recruitingAndEligibleWithNoSlots.size
            )
            return create(recruitingAndEligibleWithNoSlots, title, width)
        }

        fun forClosedCohorts(
            cohorts: List<EvaluatedCohort?>, contentWidth: Float,
            enableExtendedMode: Boolean
        ): EligibleActinTrialsGenerator {
            val unavailableAndEligible = cohorts.stream()
                .filter { trial: EvaluatedCohort? -> trial!!.isPotentiallyEligible && !trial.isOpen }
                .filter { trial: EvaluatedCohort? -> !trial!!.molecularEvents().isEmpty() || enableExtendedMode }
                .collect(Collectors.toList())
            val title = String.format(
                "%s trials and cohorts that %smay be eligible, but are closed (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                if (enableExtendedMode) "" else "meet molecular requirements and ",
                unavailableAndEligible.size
            )
            return create(unavailableAndEligible, title, contentWidth)
        }

        private fun create(cohorts: List<EvaluatedCohort?>, title: String, width: Float): EligibleActinTrialsGenerator {
            val trialColWidth = width / 9
            val cohortColWidth = width / 4
            val molecularColWidth = width / 7
            val checksColWidth = width - (trialColWidth + cohortColWidth + molecularColWidth)
            return EligibleActinTrialsGenerator(cohorts, title, trialColWidth, cohortColWidth, molecularColWidth, checksColWidth)
        }

        private fun concat(strings: Set<String?>): String {
            val concatenatedString = java.lang.String.join(Formats.COMMA_SEPARATOR, strings)
            return if (concatenatedString.isEmpty()) Formats.VALUE_NONE else concatenatedString
        }
    }
}