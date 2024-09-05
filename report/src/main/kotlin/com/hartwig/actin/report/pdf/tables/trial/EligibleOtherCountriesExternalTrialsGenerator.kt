package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleOtherCountriesExternalTrialsGenerator(
    private val sources: Set<String>,
    private val externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
    private val width: Float,
    private val filteredCount: Int
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials potentially eligible based on molecular results which are potentially recruiting internationally (%d)",
            sources.joinToString(),
            externalTrialsPerEvent.values.flatten().size
        )
    }

    override fun contents(): Table {
        val eventWidth = (0.9 * width / 5).toFloat()
        val sourceEventWidth = (0.9 * width / 5).toFloat()
        val cancerTypeWidth = (0.9 * width / 5).toFloat()
        val titleWidth = (1.5 * width / 5).toFloat()
        val countriesWidth = (0.8 * width / 5).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, sourceEventWidth + cancerTypeWidth + titleWidth + countriesWidth)

        table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Event")))
        val headerSubTable = Tables.createFixedWidthCols(sourceEventWidth, cancerTypeWidth, titleWidth, countriesWidth)
        listOf(
            "Source Event",
            "Cancer Type",
            "Trial title",
            "Country (city)"
        ).forEach { headerSubTable.addHeaderCell(Cells.createHeader(it)) }
        table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))

        externalTrialsPerEvent.forEach { (event, externalTrials) ->
            val subTable = Tables.createFixedWidthCols(sourceEventWidth, cancerTypeWidth, titleWidth, countriesWidth)
            externalTrials.forEach { externalTrial ->
                subTable.addCell(Cells.createContentNoBorder(externalTrial.sourceEvent))
                subTable.addCell(Cells.createContentNoBorder(externalTrial.applicableCancerType.cancerType))
                subTable.addCell(
                    Cells.createContentNoBorder(EligibleExternalTrialGeneratorFunctions.shortenTitle(externalTrial.title))
                        .setAction(PdfAction.createURI(externalTrial.url))
                        .addStyle(Styles.urlStyle())
                )
                subTable.addCell(Cells.createContentNoBorder(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrial)))
            }
            table.addCell(Cells.createContent(event))
            EligibleExternalTrialGeneratorFunctions.insertRow(table, subTable)
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