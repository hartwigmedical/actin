package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.TreatmentConstants
import com.itextpdf.layout.element.Table
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

class IneligibleActinTrialsGenerator private constructor(
    private val cohorts: List<EvaluatedCohort?>, private val source: String,
    private val trialColWidth: Float, private val cohortColWidth: Float, private val ineligibilityReasonColWith: Float,
    private val enableExtendedMode: Boolean
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials and cohorts that are %sconsidered ineligible (%s)",
            source,
            if (enableExtendedMode) "" else "open but ",
            cohorts.size
        )
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + ineligibilityReasonColWith)
        if (!cohorts.isEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")))
            val headerSubTable = Tables.createFixedWidthCols(
                cohortColWidth, ineligibilityReasonColWith
            )
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"))
            headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"))
            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))
        }
        ActinTrialGeneratorFunctions.streamSortedCohorts(cohorts).forEach { cohortList: List<EvaluatedCohort?>? ->
            val trialSubTable = Tables.createFixedWidthCols(
                cohortColWidth, ineligibilityReasonColWith
            )
            cohortList!!.forEach(Consumer { cohort: EvaluatedCohort? ->
                val cohortText = ActinTrialGeneratorFunctions.createCohortString(cohort)
                val ineligibilityText = if (cohort!!.fails().isEmpty()) "?" else java.lang.String.join(", ", cohort.fails())
                ActinTrialGeneratorFunctions.addContentStreamToTable(
                    Stream.of(cohortText, ineligibilityText),
                    !cohort.isOpen || !cohort.hasSlotsAvailable(),
                    trialSubTable
                )
            })
            ActinTrialGeneratorFunctions.insertTrialRow(cohortList, table, trialSubTable)
        }
        var subNote = ""
        if (cohorts.stream().anyMatch { cohort: EvaluatedCohort? -> !cohort!!.isOpen }) {
            subNote += " Cohorts shown in grey are closed or have no slots available."
        }
        if (cohorts.stream().anyMatch { cohort: EvaluatedCohort? -> cohort!!.isOpen && !cohort.hasSlotsAvailable() }) {
            subNote += " Open cohorts with no slots available are indicated by an asterisk (*)."
        }
        if (!subNote.isEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table))
        }
        return makeWrapping(table)
    }

    companion object {
        fun fromEvaluatedCohorts(
            cohorts: List<EvaluatedCohort?>, contentWidth: Float,
            enableExtendedMode: Boolean
        ): IneligibleActinTrialsGenerator {
            val ineligibleCohorts = cohorts.stream()
                .filter { cohort: EvaluatedCohort? -> !cohort!!.isPotentiallyEligible && (cohort.isOpen || enableExtendedMode) }
                .collect(Collectors.toList())
            val trialColWidth = contentWidth / 9
            val cohortColWidth = contentWidth / 4
            val ineligibilityReasonColWidth = contentWidth - (trialColWidth + cohortColWidth)
            return IneligibleActinTrialsGenerator(
                ineligibleCohorts,
                TreatmentConstants.ACTIN_SOURCE,
                trialColWidth,
                cohortColWidth,
                ineligibilityReasonColWidth,
                enableExtendedMode
            )
        }
    }
}