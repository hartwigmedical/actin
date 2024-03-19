package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

typealias ContentDefinition = Pair<List<String>, Boolean>

internal object ActinTrialGeneratorFunctions {

    fun addTrialsToTable(
        evaluatedCohorts: List<EvaluatedCohort>,
        table: Table,
        cohortColumnWidth: Float,
        molecularEventColumnWidth: Float,
        feedbackColumnWidth: Float,
        feedbackFunction: (EvaluatedCohort) -> Set<String>
    ) {
        sortedCohortGroups(evaluatedCohorts).forEach { cohortList: List<EvaluatedCohort> ->
            val trialSubTable = Tables.createFixedWidthCols(
                cohortColumnWidth, molecularEventColumnWidth, feedbackColumnWidth
            )
            contentForTrialCohortList(cohortList, feedbackFunction).forEach { (cellContent, deemphasizeContent) ->
                addContentListToTable(cellContent, deemphasizeContent, trialSubTable)
            }
            insertTrialRow(cohortList, table, trialSubTable)
        }
    }

    fun contentForTrialCohortList(
        cohorts: List<EvaluatedCohort>, feedbackFunction: (EvaluatedCohort) -> Set<String>
    ): List<ContentDefinition> {
        val commonFeedback = if (cohorts.size > 1) {
            cohorts.map(feedbackFunction).reduce { acc, set -> acc.intersect(set) }
        } else emptySet()
        val prefix = if (commonFeedback.isEmpty()) emptyList() else listOf(
            ContentDefinition(
                listOf("All cohorts", "", concat(commonFeedback)),
                false
            )
        )
        return prefix + cohorts.map { cohort: EvaluatedCohort ->
            ContentDefinition(
                listOf(cohort.cohort ?: "", concat(cohort.molecularEvents), concat(feedbackFunction.invoke(cohort) - commonFeedback)),
                !cohort.isOpen || !cohort.hasSlotsAvailable
            )
        }
    }

    private fun sortedCohortGroups(cohorts: List<EvaluatedCohort>): List<List<EvaluatedCohort>> {
        val sortedCohorts = cohorts.sortedWith(EvaluatedCohortComparator())
        val cohortsByTrialId = sortedCohorts.groupBy(EvaluatedCohort::trialId)

        return sortedCohorts.map(EvaluatedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun addContentListToTable(cellContent: List<String>, deemphasizeContent: Boolean, table: Table) {
        cellContent.map { text: String ->
            if (deemphasizeContent) {
                Cells.createContentNoBorderDeemphasize(text)
            } else {
                Cells.createContentNoBorder(text)
            }
        }.forEach(table::addCell)
    }

    private fun insertTrialRow(cohortList: List<EvaluatedCohort>, table: Table, trialSubTable: Table) {
        if (cohortList.isNotEmpty()) {
            val cohort = cohortList.first()
            val trialLabelText = listOf(
                Text(cohort.trialId.trimIndent()).addStyle(Styles.tableHighlightStyle()),
                Text("\n"),
                Text(cohort.acronym).addStyle(Styles.tableContentStyle())
            )
            table.addCell(createContent(Paragraph().addAll(trialLabelText)))
            val finalSubTable = if (trialSubTable.numberOfRows > 2) {
                Tables.makeWrapping(trialSubTable, false)
            } else {
                trialSubTable.setKeepTogether(true)
            }
            table.addCell(createContent(finalSubTable))
        }
    }

    private fun concat(strings: Set<String>): String {
        return strings.sorted().joinToString(Formats.COMMA_SEPARATOR).ifEmpty { Formats.VALUE_NONE }
    }
}