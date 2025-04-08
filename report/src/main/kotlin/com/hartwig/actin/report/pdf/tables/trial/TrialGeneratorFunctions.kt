package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortComparator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialFunctions.countryNamesWithCities
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialFunctions.hospitalsAndCitiesInCountry
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Cells.createContentNoBorder
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.kernel.pdf.action.PdfAction
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
        paddingDistance: Float = 1f,
        allowDeEmphasis: Boolean
    ) {
        sortedCohortGroups(cohorts, requestingSource).forEach { cohortList: List<InterpretedCohort> ->
            val trialSubTable = Tables.createFixedWidthCols(*tableWidths)
            ActinTrialContentFunctions.contentForTrialCohortList(
                cohorts = cohortList,
                feedbackFunction = feedbackFunction,
                includeFeedback = includeFeedback
            ).forEach { addContentListToTable(it.textEntries, it.deEmphasizeContent && allowDeEmphasis, trialSubTable, paddingDistance) }
            insertTrialRow(cohortList, table, trialSubTable, allowDeEmphasis)
        }

        externalTrials.forEach { trial ->
            val trialLabelText = trial.title.takeIf { it.length < 20 } ?: trial.nctId
            val mainContentFunction = if (allowDeEmphasis) Cells::createContentSmallItalic else Cells::createContent
            table.addCell(mainContentFunction(trialLabelText).setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle()))

            val trialSubTable = Tables.createFixedWidthCols(*tableWidths)
            val subContentFunction = if (allowDeEmphasis) Cells::createContentSmallItalicNoBorder else Cells::createContentNoBorder
            val country = if (trial.countries.none { it.country == homeCountry }) null else homeCountry

            val contentList = listOf(
                trial.sourceMolecularEvents.joinToString(", "),
                trial.actinMolecularEvents.joinToString(", "),
                externalTrialLocation(trial, country)
            )
            val content = if (contentList.size < tableWidths.size) contentList + "" else contentList
            content.map(subContentFunction).forEach(trialSubTable::addCell)
            table.addCell(createContent(wrapSubTable(trialSubTable)))
        }
    }

    private fun externalTrialLocation(trial: ExternalTrialSummary, homeCountry: Country?): String {
        return homeCountry?.let {
            val (hospitals, cities) = hospitalsAndCitiesInCountry(trial, it)
            if (homeCountry == Country.NETHERLANDS && hospitals != MANY_PLEASE_CHECK_LINK) hospitals else cities
        } ?: countryNamesWithCities(trial)
    }

    private fun sortedCohortGroups(cohorts: List<InterpretedCohort>, requestingSource: TrialSource?): List<List<InterpretedCohort>> {
        val sortedCohorts = cohorts.sortedWith(InterpretedCohortComparator(requestingSource))
        val cohortsByTrialId = sortedCohorts.groupBy(InterpretedCohort::trialId)

        return sortedCohorts.map(InterpretedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun addContentListToTable(cellContent: List<String>, deEmphasizeContent: Boolean, table: Table, paddingDistance: Float) {
        cellContent.map {
            val paragraph = Paragraph(it).setKeepTogether(true)
            val cell = if (deEmphasizeContent) Cells.createContentNoBorderDeEmphasize(paragraph) else createContentNoBorder(paragraph)
            cell.setPadding(paddingDistance)
        }.forEach(table::addCell)
    }

    private fun insertTrialRow(cohortList: List<InterpretedCohort>, table: Table, trialSubTable: Table, allowDeEmphasis: Boolean) {
        if (cohortList.isNotEmpty()) {
            val cohort = cohortList.first()
            val trialLabelText = listOfNotNull(
                Text(cohort.trialId.trimIndent()).addStyle(Styles.tableHighlightStyle()),
                if (trialIdIsNotAcronym(cohort)) Text("\n") else null,
                if (trialIdIsNotAcronym(cohort)) Text(cohort.acronym).addStyle(Styles.tableContentStyle()) else null,
                cohort.phase?.takeIf { it != TrialPhase.COMPASSIONATE_USE }
                    ?.let { Text("\n(${it.display()})").addStyle(Styles.tableContentStyle()) })

            if ((cohortList.none(InterpretedCohort::hasSlotsAvailable) || cohortList.none(InterpretedCohort::isOpen)) && allowDeEmphasis) {
                val trialLabel = trialLabelText.map { it.addStyle(Styles.deEmphasizedStyle()) }
                table.addCell(
                    cohort.url?.let {
                        createContent(Paragraph().addAll(trialLabel).setAction(PdfAction.createURI(it)).setUnderline())
                    } ?: createContent(Paragraph().addAll(trialLabel))
                )
            } else {
                table.addCell(
                    cohort.url?.let {
                        createContent(Paragraph().addAll(trialLabelText.map { label -> label.addStyle(Styles.urlStyle()) })).setAction(
                            PdfAction.createURI(it)
                        )
                    } ?: createContent(Paragraph().addAll(trialLabelText))
                )
            }
            table.addCell(createContent(wrapSubTable(trialSubTable)))
        }
    }

    private fun wrapSubTable(table: Table): Table {
        return if (table.numberOfRows > 2) {
            Tables.makeWrapping(table, false)
        } else {
            table.setKeepTogether(true)
        }
    }

    private fun trialIdIsNotAcronym(cohort: InterpretedCohort) = cohort.trialId.trimIndent() != cohort.acronym
}