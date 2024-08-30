package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleLocalExternalTrialsGenerator(
    private val sources: Set<String>,
    private val externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
    private val width: Float,
    private val filteredCount: Int,
    private val homeCountry: Country
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials potentially eligible based on molecular results which are potentially recruiting locally in %s (%d)",
            sources.joinToString(),
            homeCountry.display(),
            externalTrialsPerEvent.values.flatten().size
        )
    }

    override fun contents(): Table {
        val eventWidth = (1.1 * width / 5).toFloat()
        val sourceEventWidth = (1.1 * width / 5).toFloat()
        val cancerTypeWidth = (1.1 * width / 5).toFloat()
        val titleWidth = (2.0 * width / 5).toFloat()
        val nctWidth = (0.7 * width / 5).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, sourceEventWidth + cancerTypeWidth + titleWidth + nctWidth)
        table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Event")))
        val headerSubTable = Tables.createFixedWidthCols(sourceEventWidth, cancerTypeWidth, titleWidth, nctWidth)
        listOf("Source Event", "Cancer Type", "Trial title", "NCT number").forEach { headerSubTable.addHeaderCell(Cells.createHeader(it)) }
        table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))

        externalTrialsPerEvent.forEach { (event, externalTrials) ->
            val subTable = Tables.createFixedWidthCols(sourceEventWidth, cancerTypeWidth, titleWidth, nctWidth)
            externalTrials.forEach {
                subTable.addCell(Cells.createContentNoBorder(it.sourceEvent))
                subTable.addCell(Cells.createContentNoBorder(it.applicableCancerType.cancerType))
                subTable.addCell(Cells.createContentNoBorder(EligibleExternalTrialGeneratorFunctions.shortenTitle(it.title)))
                subTable.addCell(Cells.createContentNoBorder(it.nctId).setAction(PdfAction.createURI(it.url)).addStyle(Styles.urlStyle()))
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