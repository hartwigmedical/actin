package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
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
    private val homeCountry: CountryName? = null
) : TableGenerator {
    override fun title() =
        "${sources.joinToString()} trials potentially eligible based on molecular results which are potentially " +
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
            homeCountry?.let { if (it == CountryName.NETHERLANDS) "Hospitals" else "Cities" } ?: "Country (cities)"
        ).forEach { table.addHeaderCell(Cells.createHeader(it)) }

        trials.forEach { trial ->
            table.addCell(
                Cells.createContent(EligibleExternalTrialGeneratorFunctions.shortenTitle(trial.title))
                    .setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle())
            )
            table.addCell(Cells.createContent(trial.actinMolecularEvents.joinToString(",\n")))
            table.addCell(Cells.createContent(trial.sourceMolecularEvents.joinToString(",\n")))
            table.addCell(Cells.createContent(trial.cancerTypes.joinToString(",\n") { it.cancerType }))
            table.addCell(
                Cells.createContent(
                    homeCountry?.let {
                        EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(
                            trial,
                            it
                        ).first
                    } ?: EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(trial)
                )
            )
        }
        if (filteredCount > 0)
            table.addCell(
                Cells.createSpanningSubNote(
                    "$filteredCount trials were filtered out due to overlapping molecular targets. See extended report for all matches.",
                    table
                )
            )

        return makeWrapping(table)
    }
}