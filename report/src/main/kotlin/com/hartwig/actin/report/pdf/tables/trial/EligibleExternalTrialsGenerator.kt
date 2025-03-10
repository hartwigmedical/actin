package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleExternalTrialsGenerator(
    private val sources: Set<String>,
    private val trials: Set<ExternalTrialSummary>,
    private val width: Float,
    private val filteredCount: Int,
    private val homeCountry: Country? = null,
    private val isFilteredTrialsTable: Boolean = true
) : TableGenerator {

    override fun title() =
        "${if (isFilteredTrialsTable) "" else "Filtered"} ${sources.joinToString()} trials potentially eligible based on molecular results which are potentially " +
                "recruiting ${homeCountry?.let { "locally in ${it.display()}" } ?: "internationally"} (${trials.size})"

    override fun contents(): Table {
        val eventWidth = (0.9 * width / 5).toFloat()
        val sourceEventWidth = (0.9 * width / 5).toFloat()
        val cancerTypeWidth = (0.9 * width / 5).toFloat()
        val titleWidth = (1.5 * width / 5).toFloat()
        val hospitalsOrCitiesWidth = (0.8 * width / 5).toFloat()

        val table = Tables.createFixedWidthCols(titleWidth, eventWidth, sourceEventWidth, cancerTypeWidth, hospitalsOrCitiesWidth)
        listOf(
            "Trial title",
            "Events",
            "Source Events",
            "Cancer Types",
            homeCountry?.let { if (it == Country.NETHERLANDS) "Hospitals" else "Cities" } ?: "Country (cities)"
        ).forEach { table.addHeaderCell(Cells.createHeader(it)) }

        trials.forEach { trial ->
            table.addCell(
                Cells.createContent(EligibleExternalTrialGeneratorFunctions.shortenTitle(trial.title))
                    .setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle())
            )
            table.addCell(Cells.createContent(trial.actinMolecularEvents.joinToString(",\n")))
            table.addCell(Cells.createContent(trial.sourceMolecularEvents.joinToString(",\n")))
            table.addCell(Cells.createContent(trial.applicableCancerTypes.joinToString(",\n") { it.matchedCancerType }))
            table.addCell(
                Cells.createContent(
                    homeCountry?.let {
                        val hospitalsToCities = EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(trial, it)
                        if (homeCountry == Country.NETHERLANDS) hospitalsToCities.first else hospitalsToCities.second
                    } ?: EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(trial)
                )
            )
        }
        if (table.numberOfRows == 0) {
            table.addCell(Cells.createSpanningNoneEntry(table))
        }
        if (filteredCount > 0 && isFilteredTrialsTable)
            table.addCell(
                Cells.createSpanningSubNote(
                    homeCountry?.let {
                        "$filteredCount trials were filtered out due to eligible local trials for the same molecular target or trial for young adult patients. " +
                                "See trial matching summary for filtered matches."
                    }
                        ?: ("$filteredCount trials were filtered out due to ${sources.joinToString()} trials recruiting nationally for "
                                + "the same molecular target. See trial matching summary for filtered matches."),
                    table
                )
            )

        return makeWrapping(table)
    }
}

fun provideExternalTrialsTablesIncludedByFilter(
    trialsProvider: TrialsProvider, evaluated: List<InterpretedCohort>, contentWidth: Float, homeCountry: Country?
): Pair<TableGenerator?, TableGenerator?> {
    val summarizedExternalTrials = trialsProvider.summarizeExternalTrials(evaluated)
    val allEvidenceSources = trialsProvider.allEvidenceSources()
    return Pair(
        if (summarizedExternalTrials.nationalTrials.isNotEmpty()) {
            EligibleExternalTrialsGenerator(
                allEvidenceSources,
                summarizedExternalTrials.nationalTrials.filtered,
                contentWidth,
                summarizedExternalTrials.nationalTrials.originalMinusFilteredSize(),
                homeCountry
            )
        } else null,
        if (summarizedExternalTrials.internationalTrials.isNotEmpty()) {
            EligibleExternalTrialsGenerator(
                allEvidenceSources,
                summarizedExternalTrials.internationalTrials.filtered,
                contentWidth,
                summarizedExternalTrials.internationalTrials.originalMinusFilteredSize()
            )
        } else null
    )
}

fun provideExternalTrialsTablesExcludedByFilter(
    trialsProvider: TrialsProvider, evaluated: List<InterpretedCohort>, contentWidth: Float, homeCountry: Country?
): Pair<TableGenerator?, TableGenerator?> {
    val summarizedExternalTrials = trialsProvider.summarizeExternalTrials(evaluated)
    val allEvidenceSources = trialsProvider.allEvidenceSources()
    return Pair(
        if (summarizedExternalTrials.nationalTrials.originalMinusFiltered().isNotEmpty()) {
            EligibleExternalTrialsGenerator(
                allEvidenceSources,
                summarizedExternalTrials.nationalTrials.originalMinusFiltered(),
                contentWidth,
                summarizedExternalTrials.nationalTrials.originalMinusFilteredSize(),
                homeCountry,
                false
            )
        } else null,
        if (summarizedExternalTrials.internationalTrials.originalMinusFiltered().isNotEmpty()) {
            EligibleExternalTrialsGenerator(
                allEvidenceSources,
                summarizedExternalTrials.internationalTrials.originalMinusFiltered(),
                contentWidth,
                summarizedExternalTrials.internationalTrials.originalMinusFilteredSize(),
                isFilteredTrialsTable = false
            )
        } else null
    )
}