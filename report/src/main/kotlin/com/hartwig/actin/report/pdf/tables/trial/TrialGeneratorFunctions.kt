package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object TrialGeneratorFunctions {

    fun addTrialsToTable(
        cohorts: List<InterpretedCohort>,
        externalTrials: Set<ExternalTrialSummary>,
        requestingSource: TrialSource?,
        homeCountry: Country?,
        table: Table,
        tableWidths: FloatArray,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeFeedback: Boolean = true,
        paddingDistance: Float = 1f
    ) {
        sortedCohortGroups(cohorts, requestingSource).forEach { cohortList: List<InterpretedCohort> ->
            val trialSubTable = Tables.createFixedWidthCols(*tableWidths)
            ActinTrialContentFunctions.contentForTrialCohortList(
                cohorts = cohortList,
                feedbackFunction = feedbackFunction,
                includeFeedback = includeFeedback
            ).forEach { addContentListToTable(it.textEntries, it.deEmphasizeContent, trialSubTable, paddingDistance) }
            insertTrialRow(cohortList, table, trialSubTable)
        }

        externalTrials.forEach { trial ->
            table.addCell(
                createContent(EligibleExternalTrialGeneratorFunctions.shortenTitle(trial.title))
                    .setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle())
            )
            table.addCell(createContent(trial.sourceMolecularEvents.joinToString(",\n")))
            table.addCell(createContent(trial.actinMolecularEvents.joinToString(",\n")))
            table.addCell(
                createContent(
                     homeCountry?.let {
                        val hospitalsToCities = EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(trial, it)
                        if (homeCountry == Country.NETHERLANDS) hospitalsToCities.first else hospitalsToCities.second
                    } ?: EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(trial)
                )
            )
        }
    }

    private fun sortedCohortGroups(cohorts: List<InterpretedCohort>, requestingSource: TrialSource?): List<List<InterpretedCohort>> {
        val sortedCohorts = cohorts.sortedWith(InterpretedCohortComparator(requestingSource))
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

    private fun renderTrialTitle(trialLabelText: List<Text>, cohort: InterpretedCohort): Cell {
        return if (!cohort.url.isNullOrBlank()) {
            createContent(Paragraph().addAll(trialLabelText.map { it.addStyle(Styles.urlStyle()) })).setAction(
                PdfAction.createURI(cohort.url)
            )
        } else createContent(Paragraph().addAll(trialLabelText))
    }

    private fun insertTrialRow(cohortList: List<InterpretedCohort>, table: Table, trialSubTable: Table) {
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
                            PdfAction.createURI(cohort.url)
                        )
                    } ?: createContent(Paragraph().addAll(trialLabelText))

                    else -> renderTrialTitle(trialLabelText, cohort)
                }
            )
            val finalSubTable = if (trialSubTable.numberOfRows > 2) {
                Tables.makeWrapping(trialSubTable, false)
            } else {
                trialSubTable.setKeepTogether(true)
            }
            table.addCell(createContent(finalSubTable))
        }
    }

    private fun trialIdIsNotAcronym(cohort: InterpretedCohort) = cohort.trialId.trimIndent() != cohort.acronym
}