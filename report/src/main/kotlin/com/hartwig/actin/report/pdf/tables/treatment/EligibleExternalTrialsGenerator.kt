package com.hartwig.actin.report.pdf.tables.treatment

import com.google.common.collect.Multimap
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.COMMA_SEPARATOR
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleExternalTrialsGenerator(
    private val source: String, private val externalTrialsPerEvent: Multimap<String, ExternalTrial>,
    private val width: Float
) : TableGenerator {
    override fun title(): String {
        return String.format("%s trials potentially eligible based on molecular results (%d)", source, externalTrialsPerEvent.size())
    }

    override fun contents(): Table {
        val colWidth = width / 5
        val table = Tables.createFixedWidthCols((0.9*colWidth).toFloat(), (colWidth*2.2).toFloat(), (0.7*colWidth).toFloat(), (1.2*colWidth).toFloat())
        table.addHeaderCell(Cells.createHeader("Event"))
        table.addHeaderCell(Cells.createHeader("Trial title"))
        table.addHeaderCell(Cells.createHeader("NCT id"))
        table.addHeaderCell(Cells.createHeader("Potentially recruiting countries"))

        externalTrialsPerEvent.forEach { event, eligibleTrial ->
            table.addCell(Cells.createContent(event))
            table.addCell(Cells.createContent(shortenTitle(eligibleTrial.title())))
            table.addCell(Cells.createContent(eligibleTrial.website().takeLast(11)).setAction(PdfAction.createURI(eligibleTrial.website())).addStyle(
                Styles.urlStyle()))
            table.addCell(Cells.createContent(eligibleTrial.countries().joinToString(COMMA_SEPARATOR)))
        }
        return makeWrapping(table)
    }

    private fun shortenTitle(title: String): String {
        return if (title.length > 170) {
            title.take(85).substringBeforeLast(" ") + " ... " + title.takeLast(85).substringAfter(" ")
        } else {
            title
        }
    }
}