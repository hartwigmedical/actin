package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

internal object ActinTrialGeneratorFunctions {
    fun streamSortedCohorts(cohorts: List<EvaluatedCohort?>): Stream<List<EvaluatedCohort?>> {
        cohorts.sort(EvaluatedCohortComparator())
        val cohortsByTrialId = cohorts.stream().collect(
            Collectors.groupingBy(
                Function { obj: EvaluatedCohort? -> obj!!.trialId() })
        )
        return cohorts.stream().map { obj: EvaluatedCohort? -> obj!!.trialId() }
            .distinct().map { key: String -> cohortsByTrialId[key] }
    }

    fun createCohortString(cohort: EvaluatedCohort?): String {
        return if (cohort!!.cohort() == null) "" else cohort.cohort()!!
    }

    fun addContentStreamToTable(cellContent: Stream<String?>, deemphasizeContent: Boolean, table: Table) {
        cellContent.map<Cell>(Function<String, Cell> { text: String ->
            if (deemphasizeContent) {
                return@map Cells.createContentNoBorderDeemphasize(text)
            } else {
                return@map Cells.createContentNoBorder(text)
            }
        }).forEach { cell: Cell? -> table.addCell(cell) }
    }

    fun insertTrialRow(cohortList: List<EvaluatedCohort?>?, table: Table, trialSubTable: Table) {
        var trialSubTable = trialSubTable
        if (!cohortList!!.isEmpty()) {
            val cohort = cohortList[0]
            table.addCell(
                createContent(
                    Cells.createContentNoBorder(
                        Paragraph().addAll(
                            java.util.List.of(
                                Text(
                                    """
                    ${cohort!!.trialId()}
                    
                    """.trimIndent()
                                ).addStyle(Styles.tableHighlightStyle()),
                                Text(cohort.acronym()).addStyle(Styles.tableContentStyle())
                            )
                        )
                    )
                )
            )
            if (trialSubTable.numberOfRows > 2) {
                trialSubTable = Tables.makeWrapping(trialSubTable, false)
            } else {
                trialSubTable.setKeepTogether(true)
            }
            table.addCell(createContent(trialSubTable))
        }
    }
}