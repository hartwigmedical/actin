package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.report.interpretation.InterpretedCohortComparator
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object ActinTrialGeneratorFunctions {

    fun addTrialsToTable(
        cohorts: List<InterpretedCohort>,
        table: Table,
        cohortColumnWidth: Float,
        molecularEventColumnWidth: Float,
        feedbackColumnWidth: Float,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        hasFeedback: Boolean = true
    ) {
        sortedCohortGroups(cohorts).forEach { cohortList: List<InterpretedCohort> ->
            val trialSubTable = if (hasFeedback) { Tables.createFixedWidthCols(cohortColumnWidth, molecularEventColumnWidth, feedbackColumnWidth) } else {Tables.createFixedWidthCols(cohortColumnWidth, molecularEventColumnWidth)}
            ActinTrialContentFunctions.contentForTrialCohortList(cohortList, feedbackFunction, hasFeedback)
                .forEach { addContentListToTable(it.textEntries, it.deEmphasizeContent, trialSubTable) }

            insertTrialRow(cohortList, table, trialSubTable)
        }
    }

    private fun sortedCohortGroups(cohorts: List<InterpretedCohort>): List<List<InterpretedCohort>> {
        val sortedCohorts = cohorts.sortedWith(InterpretedCohortComparator())
        val cohortsByTrialId = sortedCohorts.groupBy(InterpretedCohort::trialId)

        return sortedCohorts.map(InterpretedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun addContentListToTable(cellContent: List<String>, deEmphasizeContent: Boolean, table: Table) {
        cellContent.map {
            val paragraph = Paragraph(it).setKeepTogether(true)
            if (deEmphasizeContent) Cells.createContentNoBorderDeEmphasize(paragraph) else Cells.createContentNoBorder(paragraph)
        }.forEach(table::addCell)
    }

    private fun insertTrialRow(cohortList: List<InterpretedCohort>, table: Table, trialSubTable: Table) {
        if (cohortList.isNotEmpty()) {
            val cohort = cohortList.first()
            val trialLabelText = listOfNotNull(
                Text(cohort.trialId.trimIndent()).addStyle(Styles.tableHighlightStyle()),
                Text("\n"),
                Text(cohort.acronym).addStyle(Styles.tableContentStyle()),
                cohort.phase?.takeIf { it != TrialPhase.COMPASSIONATE_USE }
                    ?.let { Text("\n(${it.display()})").addStyle(Styles.tableContentStyle()) }
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