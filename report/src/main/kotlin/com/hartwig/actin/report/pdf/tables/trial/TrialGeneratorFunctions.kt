package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

data class ContentDefinition(val textEntries: List<String>, val deEmphasizeContent: Boolean)

object TrialGeneratorFunctions {

    private const val SMALL_LINE_DISTANCE = 0.9f

    fun addTrialsToTable(
        table: Table,
        cohorts: List<InterpretedCohort>,
        externalTrials: Set<ExternalTrialSummary>,
        requestingSource: TrialSource?,
        countryOfReference: Country?,
        includeFeedback: Boolean,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        allowDeEmphasis: Boolean,
        useSmallerSize: Boolean,
        includeCohortConfig: Boolean,
        includeSites: Boolean,
    ) {
        sortedCohortsGroupedByTrial(cohorts, requestingSource).forEach { cohortList: List<InterpretedCohort> ->
            insertAllCohortsForTrial(
                table,
                cohortList,
                requestingSource,
                includeFeedback,
                feedbackFunction,
                allowDeEmphasis,
                useSmallerSize,
                includeCohortConfig,
                includeSites
            )
        }

        externalTrials.forEach { trial ->
            val trialLabelText = trial.title.takeIf { it.length < 20 } ?: trial.nctId
            val contentFunction = when {
                useSmallerSize && allowDeEmphasis -> Cells::createContentSmallItalic
                useSmallerSize -> Cells::createContentSmall
                allowDeEmphasis -> Cells::createContentMediumItalic
                else -> Cells::createContent
            }
            table.addCell(contentFunction(trialLabelText).setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle()))
            table.addCell(contentFunction(trial.sourceMolecularEvents.joinToString(", ")))
            table.addCell(contentFunction(trial.actinMolecularEvents.joinToString(", ")))

            val country = if (trial.countries.none { it.country == countryOfReference }) null else countryOfReference
            table.addCell(contentFunction(TrialLocations.externalTrialLocation(trial, country)))
            if (includeFeedback) {
                table.addCell(contentFunction(""))
            }
        }
    }

    private fun sortedCohortsGroupedByTrial(
        cohorts: List<InterpretedCohort>,
        requestingSource: TrialSource?
    ): List<List<InterpretedCohort>> {
        val sortedCohorts = cohorts.sortedWith(InterpretedCohortComparator(requestingSource))
        val cohortsByTrialId = sortedCohorts.groupBy(InterpretedCohort::trialId)

        return sortedCohorts.map(InterpretedCohort::trialId).distinct().mapNotNull { cohortsByTrialId[it] }
    }

    private fun insertAllCohortsForTrial(
        table: Table,
        cohortsForTrial: List<InterpretedCohort>,
        requestingSource: TrialSource?,
        includeFeedback: Boolean,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        allowDeEmphasis: Boolean,
        useSmallerSize: Boolean,
        includeCohortConfig: Boolean,
        includeSites: Boolean
    ) {
        table.addCell(generateTrialTitleCell(cohortsForTrial, allowDeEmphasis, useSmallerSize).setKeepTogether(true))

        contentForTrialCohortList(
            cohortsForTrial = cohortsForTrial,
            includeFeedback = includeFeedback,
            feedbackFunction = feedbackFunction,
            requestingSource = requestingSource,
            includeCohortConfig = includeCohortConfig,
            includeSites = includeSites
        ).forEachIndexed { index, content ->
            addContentListToTable(
                table,
                index == 0,
                content.textEntries,
                content.deEmphasizeContent && allowDeEmphasis,
                useSmallerSize
            )
        }
    }

    private fun generateTrialTitleCell(
        cohortsForTrial: List<InterpretedCohort>,
        allowDeEmphasis: Boolean,
        useSmallerSize: Boolean
    ): Cell {
        val anyCohort = cohortsForTrial.first()
        val trialIdIsNotAcronym = anyCohort.trialId.trimIndent() != anyCohort.acronym
        val trialLabelText = listOfNotNull(
            Text(anyCohort.trialId.trimIndent()).addStyle(Styles.tableHighlightStyle()),
            if (trialIdIsNotAcronym) Text("\n") else null,
            if (trialIdIsNotAcronym) Text(anyCohort.acronym).addStyle(Styles.tableContentStyle()) else null,
            anyCohort.phase?.takeIf { it != TrialPhase.COMPASSIONATE_USE }
                ?.let { Text("\n(${it.display()})").addStyle(Styles.tableContentStyle()) })

        val hasNoOpenCohortsWithSlots =
            (cohortsForTrial.none(InterpretedCohort::hasSlotsAvailable) || cohortsForTrial.none(InterpretedCohort::isOpen))

        val paragraph = if (useSmallerSize) Paragraph().setMultipliedLeading(SMALL_LINE_DISTANCE) else Paragraph()
        val fontSize = if (useSmallerSize) Styles.SMALL_FONT_SIZE else Styles.REGULAR_FONT_SIZE
        return if (hasNoOpenCohortsWithSlots && allowDeEmphasis) {
            val trialLabel = trialLabelText.map { it.addStyle(Styles.deEmphasizedStyle()).setFontSize(fontSize) }
            anyCohort.url?.let {
                Cells.createContent(paragraph.addAll(trialLabel).setAction(PdfAction.createURI(it)).setUnderline())
            } ?: Cells.createContent(paragraph.addAll(trialLabel))
        } else {
            val trialLabels = trialLabelText.map { it.setFontSize(fontSize) }
            anyCohort.url?.let {
                Cells.createContent(paragraph.addAll(trialLabels.map { label -> label.addStyle(Styles.urlStyle()) }))
                    .setAction(PdfAction.createURI(it))
            } ?: Cells.createContent(paragraph.addAll(trialLabels))
        }
    }

    private fun addContentListToTable(
        table: Table,
        rowContainsTrialIdentificationCell: Boolean,
        cellContentsForRow: List<String>,
        deEmphasizeContent: Boolean,
        useSmallerSize: Boolean
    ) {
        if (!rowContainsTrialIdentificationCell) {
            table.addCell(Cells.createEmpty())
        }

        cellContentsForRow.map {
            val fontSize = if (useSmallerSize) Styles.SMALL_FONT_SIZE else Styles.REGULAR_FONT_SIZE
            val paragraph = if (it.startsWith(Formats.ITALIC_TEXT_MARKER) && it.endsWith(Formats.ITALIC_TEXT_MARKER)) {
                Paragraph(it.removeSurrounding(Formats.ITALIC_TEXT_MARKER))
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)).setMultipliedLeading(1.6f).setFontSize(fontSize)
            } else {
                val suffixes = listOf("(no slots)", "(closed)")
                val text = suffixes.find { suffix -> it.endsWith(suffix) }?.let { suffix ->
                    val mainText = it.removeSuffix(suffix)
                    Paragraph().add(Text(mainText)).add(Text(suffix).setUnderline())
                } ?: Paragraph(Text(it))
                text.setFontSize(fontSize)
            }

            val cell = when {
                deEmphasizeContent && useSmallerSize -> Cells.createContentDeEmphasize(paragraph.setMultipliedLeading(SMALL_LINE_DISTANCE))
                deEmphasizeContent -> Cells.createContentDeEmphasize(paragraph)
                useSmallerSize -> Cells.createContent(paragraph.setMultipliedLeading(SMALL_LINE_DISTANCE))
                else -> Cells.createContent(paragraph)
            }
            if (!rowContainsTrialIdentificationCell) {
                cell.setBorder(Border.NO_BORDER)
            }
            cell
        }.forEach(table::addCell)
    }

    fun contentForTrialCohortList(
        cohortsForTrial: List<InterpretedCohort>,
        includeFeedback: Boolean,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeCohortConfig: Boolean,
        requestingSource: TrialSource? = null,
        includeSites: Boolean
    ): List<ContentDefinition> {
        val commonFeedback = if (includeFeedback) findCommonMembersInCohorts(cohortsForTrial, feedbackFunction) else emptySet()
        val commonEvents = findCommonMembersInCohorts(cohortsForTrial, InterpretedCohort::molecularEvents)
        val commonLocations = findCommonMembersInCohorts(cohortsForTrial, InterpretedCohort::locations)
        val allEventsEmpty = cohortsForTrial.all { it.molecularEvents.isEmpty() }

        val hidePrefix = (commonFeedback.isEmpty() && commonEvents.isEmpty() && commonLocations.isEmpty()) || cohortsForTrial.size == 1

        val prefix = if (hidePrefix) emptyList() else {
            val deEmphasizeContent = cohortsForTrial.all { !it.isOpen || !it.hasSlotsAvailable }
            listOf(
                ContentDefinition(
                    listOfNotNull(
                        "${Formats.ITALIC_TEXT_MARKER}Applies to all cohorts below${Formats.ITALIC_TEXT_MARKER}",
                        concat(commonEvents, allEventsEmpty && includeFeedback),
                        if (includeSites) TrialLocations.actinTrialLocation(
                            cohortsForTrial.first().source,
                            requestingSource,
                            commonLocations,
                            true
                        ) else null,
                        concat(commonFeedback).takeIf { includeFeedback }
                    ),
                    deEmphasizeContent
                )
            )
        }

        return prefix + cohortsForTrial.map { cohort: InterpretedCohort ->
            val cohortString = when {
                !cohort.isOpen -> cohort.name?.plus(" (closed)") ?: ""
                !cohort.hasSlotsAvailable -> cohort.name?.plus(" (no slots)") ?: ""
                else -> cohort.name ?: ""
            }

            ContentDefinition(
                listOfNotNull(
                    cohortString,
                    concat(cohort.molecularEvents - commonEvents, commonEvents.isEmpty() && (!allEventsEmpty || hidePrefix)),
                    if (includeSites) TrialLocations.actinTrialLocation(
                        cohort.source,
                        requestingSource,
                        cohort.locations - commonLocations,
                        true
                    ) else null,
                    if (includeFeedback) concat(feedbackFunction(cohort) - commonFeedback, commonFeedback.isEmpty()) else null,
                    if (includeCohortConfig) concat(
                        setOfNotNull(
                            "Ignored".takeIf { cohort.ignore },
                            "Non-evaluable".takeIf { !cohort.isEvaluable }), separator = " and "
                    ) else null,
                ),
                !cohort.isOpen || !cohort.hasSlotsAvailable
            )
        }
    }

    private fun findCommonMembersInCohorts(
        cohorts: List<InterpretedCohort>, retrieveMemberFunction: (InterpretedCohort) -> Set<String>
    ): Set<String> {
        return if (cohorts.size > 1) {
            cohorts.map(retrieveMemberFunction).reduce { acc, set -> acc.intersect(set) }
        } else emptySet()
    }

    private fun concat(strings: Set<String>, replaceEmptyWithNone: Boolean = true, separator: String = Formats.COMMA_SEPARATOR): String {
        val joinedString = strings.sorted().joinToString(separator)
        return if (replaceEmptyWithNone && joinedString.isEmpty()) Formats.VALUE_NONE else joinedString
    }
}