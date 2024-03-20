package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object ActinTrialGeneratorFunctions {

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
            ActinTrialContentFunctions.contentForTrialCohortList(cohortList, feedbackFunction)
                .forEach { addContentListToTable(it.textEntries, it.deEmphasizeContent, trialSubTable) }
            
            insertTrialRow(cohortList, table, trialSubTable)
        }
    }

    private fun sortedCohortGroups(cohorts: List<EvaluatedCohort>): List<List<EvaluatedCohort>> {
        val sortedCohorts = cohorts.sortedWith(EvaluatedCohortComparator())
        val cohortsByTrialId = sortedCohorts.groupBy(EvaluatedCohort::trialId)

        return sortedCohorts.map(EvaluatedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun addContentListToTable(cellContent: List<String>, deEmphasizeContent: Boolean, table: Table) {
        cellContent.map { text: String ->
            if (deEmphasizeContent) {
                Cells.createContentNoBorderDeEmphasize(text)
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
}