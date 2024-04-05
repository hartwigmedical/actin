package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleDutchExternalTrialsGenerator(
    private val source: String,
    private val externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
    private val width: Float,
    private val filteredCount: Int
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials potentially eligible based on molecular results which are potentially recruiting in The Netherlands (%d)",
            source,
            externalTrialsPerEvent.values.flatten().size
        )
    }

    override fun contents(): Table {
        val eventWidth = (0.9 * width / 5).toFloat()
        val titleWidth = (3.4 * width / 5).toFloat()
        val nctWidth = (0.7 * width / 5).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, titleWidth + nctWidth)
        table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Event")))
        val headerSubTable = Tables.createFixedWidthCols(titleWidth, nctWidth)
        listOf("Trial title", "NCT number").forEach { headerSubTable.addHeaderCell(Cells.createHeader(it)) }
        table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))

        externalTrialsPerEvent.forEach { (event, externalTrials) ->
            val subTable = Tables.createFixedWidthCols(titleWidth, nctWidth)
            externalTrials.forEach {
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