package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.report.interpretation.Cohort
import com.hartwig.actin.report.interpretation.CohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object ActinTrialGeneratorFunctions {

    fun addTrialsToTable(
        cohorts: List<Cohort>,
        table: Table,
        cohortColumnWidth: Float,
        molecularEventColumnWidth: Float,
        feedbackColumnWidth: Float,
        feedbackFunction: (Cohort) -> Set<String>
    ) {
        sortedCohortGroups(cohorts).forEach { cohortList: List<Cohort> ->
            val trialSubTable = Tables.createFixedWidthCols(
                cohortColumnWidth, molecularEventColumnWidth, feedbackColumnWidth
            )
            ActinTrialContentFunctions.contentForTrialCohortList(cohortList, feedbackFunction)
                .forEach { addContentListToTable(it.textEntries, it.deEmphasizeContent, trialSubTable) }

            insertTrialRow(cohortList, table, trialSubTable)
        }
    }

    private fun sortedCohortGroups(cohorts: List<Cohort>): List<List<Cohort>> {
        val sortedCohorts = cohorts.sortedWith(CohortComparator())
        val cohortsByTrialId = sortedCohorts.groupBy(Cohort::trialId)

        return sortedCohorts.map(Cohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun addContentListToTable(cellContent: List<String>, deEmphasizeContent: Boolean, table: Table) {
        cellContent.map {
            val paragraph = Paragraph(it).setKeepTogether(true)
            if (deEmphasizeContent) Cells.createContentNoBorderDeEmphasize(paragraph) else Cells.createContentNoBorder(paragraph)
        }.forEach(table::addCell)
    }

    private fun insertTrialRow(cohortList: List<Cohort>, table: Table, trialSubTable: Table) {
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