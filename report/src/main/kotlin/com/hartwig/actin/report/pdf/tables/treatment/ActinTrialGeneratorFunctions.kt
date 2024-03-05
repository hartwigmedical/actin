package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

internal object ActinTrialGeneratorFunctions {
    fun sortedCohortGroups(cohorts: List<EvaluatedCohort>): List<List<EvaluatedCohort>> {
        val sortedCohorts = cohorts.sortedWith(EvaluatedCohortComparator())
        val cohortsByTrialId = sortedCohorts.groupBy(EvaluatedCohort::trialId)

        return sortedCohorts.map(EvaluatedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    fun createCohortString(cohort: EvaluatedCohort): String {
        return cohort.cohort ?: ""
    }

    fun addContentListToTable(cellContent: List<String>, deemphasizeContent: Boolean, table: Table) {
        cellContent.map { text: String ->
            if (deemphasizeContent) {
                Cells.createContentNoBorderDeemphasize(text)
            } else {
                Cells.createContentNoBorder(text)
            }
        }.forEach(table::addCell)
    }

    fun insertTrialRow(cohortList: List<EvaluatedCohort>, table: Table, trialSubTable: Table) {
        if (cohortList.isNotEmpty()) {
            val cohort = cohortList[0]
            table.addCell(
                createContent(
                    Paragraph().addAll(
                        listOf(
                            Text(cohort.trialId.trimIndent()).addStyle(Styles.tableHighlightStyle()),
                            Text("\n"),
                            Text(cohort.acronym).addStyle(Styles.tableContentStyle())
                        )
                    )
                )
            )
            val finalSubTable = if (trialSubTable.numberOfRows > 2) {
                Tables.makeWrapping(trialSubTable, false)
            } else {
                trialSubTable.setKeepTogether(true)
            }
            table.addCell(createContent(finalSubTable))
        }
    }
}