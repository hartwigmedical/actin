package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortComparator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialFunctions.countryNamesWithCities
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialFunctions.hospitalsAndCitiesInCountry
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

const val MAX_TO_DISPLAY = 3
const val MANY_SEE_LINK = "3+ locations - see link"

object TrialGeneratorFunctions {

    fun addTrialsToTable(
        table: Table,
        cohorts: List<InterpretedCohort>,
        externalTrials: Set<ExternalTrialSummary>,
        requestingSource: TrialSource?,
        countryOfReference: Country?,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeFeedback: Boolean = true,
        paddingDistance: Float = 1f,
        allowDeEmphasis: Boolean
    ) {
        sortedCohortGroups(cohorts, requestingSource).forEach { cohortList: List<InterpretedCohort> ->
            insertTrial(table, cohortList, requestingSource, feedbackFunction, includeFeedback, paddingDistance, allowDeEmphasis)
        }

        externalTrials.forEach { trial ->
            val trialLabelText = trial.title.takeIf { it.length < 20 } ?: trial.nctId
            val mainContentFunction = if (allowDeEmphasis) Cells::createContentSmallItalic else Cells::createContent
            table.addCell(mainContentFunction(trialLabelText).setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle()))
            
            val subContentFunction = if (allowDeEmphasis) Cells::createContentSmallItalicNoBorder else Cells::createContentNoBorder
            
            table.addCell(subContentFunction(trial.sourceMolecularEvents.joinToString(", ")))
            table.addCell(subContentFunction(trial.actinMolecularEvents.joinToString(", ")))
            
            val country = if (trial.countries.none { it.country == countryOfReference }) null else countryOfReference
            table.addCell(subContentFunction(externalTrialLocation(trial, country)))
            if (includeFeedback) {
                table.addCell(Cells.createEmpty())
            }
        }
    }

    private fun externalTrialLocation(trial: ExternalTrialSummary, countryOfReference: Country?): String {
        return countryOfReference?.let {
            val (hospitals, cities) = hospitalsAndCitiesInCountry(trial, it)
            if (countryOfReference == Country.NETHERLANDS && hospitals != MANY_SEE_LINK) hospitals else cities
        } ?: countryNamesWithCities(trial)
    }

    private fun sortedCohortGroups(cohorts: List<InterpretedCohort>, requestingSource: TrialSource?): List<List<InterpretedCohort>> {
        val sortedCohorts = cohorts.sortedWith(InterpretedCohortComparator(requestingSource))
        val cohortsByTrialId = sortedCohorts.groupBy(InterpretedCohort::trialId)

        return sortedCohorts.map(InterpretedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun insertTrial(
        table: Table,
        cohortList: List<InterpretedCohort>,
        requestingSource: TrialSource?,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeFeedback: Boolean = true,
        paddingDistance: Float = 1f,
        allowDeEmphasis: Boolean
    ) {
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
                        Cells.createContent(Paragraph().addAll(trialLabel).setAction(PdfAction.createURI(it)).setUnderline())
                    } ?: Cells.createContent(Paragraph().addAll(trialLabel))
                )
            } else {
                table.addCell(
                    cohort.url?.let {
                        Cells.createContent(Paragraph().addAll(trialLabelText.map { label -> label.addStyle(Styles.urlStyle()) }))
                            .setAction(PdfAction.createURI(it))
                    } ?: Cells.createContent(Paragraph().addAll(trialLabelText))
                )
            }
            ActinTrialContentFunctions.contentForTrialCohortList(
                cohorts = cohortList,
                feedbackFunction = feedbackFunction,
                includeFeedback = includeFeedback,
                requestingSource = requestingSource
            ).forEachIndexed { index, content ->
                addContentListToTable(
                    table,
                    index == 0,
                    content.textEntries,
                    content.deEmphasizeContent && allowDeEmphasis,
                    paddingDistance
                )
            }
        }
    }

    private fun addContentListToTable(
        table: Table,
        rowContainsTrialIdentificationCell: Boolean,
        cellContentsForRow: List<String>,
        deEmphasizeContent: Boolean,
        paddingDistance: Float
    ) {
        if (!rowContainsTrialIdentificationCell) {
            table.addCell(Cells.createEmpty())
        }
        
        cellContentsForRow.map {
            val paragraph = if (it.startsWith(Formats.ITALIC_TEXT_MARKER) && it.endsWith(Formats.ITALIC_TEXT_MARKER)) {
                Paragraph(it.removeSurrounding(Formats.ITALIC_TEXT_MARKER))
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)).setMultipliedLeading(1.6f)
            } else {
                Paragraph(it)
            }

            val cell = if (deEmphasizeContent) Cells.createContentDeEmphasize(paragraph) else Cells.createContent(paragraph)
            if (!rowContainsTrialIdentificationCell) {
                cell.setBorder(Border.NO_BORDER)
            }
            cell.setPadding(paddingDistance)
        }.forEach(table::addCell)
    }

    private fun trialIdIsNotAcronym(cohort: InterpretedCohort) = cohort.trialId.trimIndent() != cohort.acronym
}