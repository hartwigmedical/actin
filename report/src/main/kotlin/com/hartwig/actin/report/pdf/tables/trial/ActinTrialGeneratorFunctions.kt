package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object ActinTrialGeneratorFunctions {

    fun addTrialsToTable(
        cohorts: List<InterpretedCohort>,
        table: Table,
        tableWidths: FloatArray,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeFeedback: Boolean = true,
        paddingDistance: Float = 1f,
        includeLocation: Boolean = false
    ) {
        sortedCohortGroups(cohorts).forEach { cohortList: List<InterpretedCohort> ->
            val trialSubTable = Tables.createFixedWidthCols(*tableWidths)
            ActinTrialContentFunctions.contentForTrialCohortList(
                cohorts = cohortList,
                feedbackFunction = feedbackFunction,
                includeLocation = includeLocation,
                includeFeedback = includeFeedback
            ).forEach { addContentListToTable(it.textEntries, it.deEmphasizeContent, trialSubTable, paddingDistance) }
            insertTrialRow(cohortList, table, trialSubTable, includeLocation)
        }
    }

    fun createTableTitleStart(source: String?): String {
        return source?.let { "$it trials" } ?: "Trials"
    }

    fun partitionBySource(cohorts: List<InterpretedCohort>, source: TrialSource?) =
        cohorts.partition { it.source == source || it.source == null || source == null }

    private fun sortedCohortGroups(cohorts: List<InterpretedCohort>): List<List<InterpretedCohort>> {
        val sortedCohorts = cohorts.sortedWith(InterpretedCohortComparator())
        val cohortsByTrialId = sortedCohorts.groupBy(InterpretedCohort::trialId)

        return sortedCohorts.map(InterpretedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun addContentListToTable(cellContent: List<String>, deEmphasizeContent: Boolean, table: Table, paddingDistance: Float) {
        cellContent.map {
            val paragraph = Paragraph(it).setKeepTogether(true)
            val cell = if (deEmphasizeContent) Cells.createContentNoBorderDeEmphasize(paragraph) else Cells.createContentNoBorder(paragraph)
            cell.setPadding(paddingDistance)
        }.forEach(table::addCell)
    }

    private fun renderTrialTitle(trialLabelText: List<Text>, cohort: InterpretedCohort, asClinicalTrialsGovLink: Boolean = false): Cell {
        return if (asClinicalTrialsGovLink && cohort.nctId?.isNotBlank() == true) {
            createContent(Paragraph().addAll(trialLabelText.map { it.addStyle(Styles.urlStyle()) })).setAction(
                PdfAction.createURI("https://clinicaltrials.gov/study/${cohort.nctId}")
            )
        } else createContent(Paragraph().addAll(trialLabelText))
    }

    private fun insertTrialRow(cohortList: List<InterpretedCohort>, table: Table, trialSubTable: Table, includeLocation: Boolean = false) {
        if (cohortList.isNotEmpty()) {
            val cohort = cohortList.first()
            val trialLabelText = listOfNotNull(
                Text(cohort.trialId.trimIndent()).addStyle(Styles.tableHighlightStyle()),
                if (trialIdIsNotAcronym(cohort)) Text("\n") else null,
                if (trialIdIsNotAcronym(cohort)) Text(cohort.acronym).addStyle(Styles.tableContentStyle()) else null,
                cohort.phase?.takeIf { it != TrialPhase.COMPASSIONATE_USE }
                    ?.let { Text("\n(${it.display()})").addStyle(Styles.tableContentStyle()) })


            table.addCell(
                when (cohort.source) {
                    TrialSource.LKO -> cohort.sourceId?.let {
                        createContent(Paragraph().addAll(trialLabelText.map { it.addStyle(Styles.urlStyle()) })).setAction(
                            PdfAction.createURI("https://longkankeronderzoek.nl/studies/${cohort.sourceId}")
                        )
                    } ?: createContent(Paragraph().addAll(trialLabelText))

                    else -> renderTrialTitle(trialLabelText, cohort, includeLocation)
                }
            )
            val finalSubTable = if (trialSubTable.numberOfRows > 2) {
                Tables.makeWrapping(trialSubTable)
            } else {
                trialSubTable.setKeepTogether(true)
            }
            table.addCell(createContent(finalSubTable))
        }
    }

    private fun trialIdIsNotAcronym(cohort: InterpretedCohort) = cohort.trialId.trimIndent() != cohort.acronym
}