package com.hartwig.actin.report.pdf.tables.treatment

import com.google.common.collect.Multimap
import com.hartwig.actin.molecular.datamodel.evidence.EligibleTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.COMMA_SEPARATOR
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleExternalTrialsGenerator(
    private val source: String, private val externalTrialsPerEvent: Multimap<String, EligibleTrial>,
    private val width: Float
) : TableGenerator {
    override fun title(): String {
        return String.format("%s trials potentially eligible based on molecular results (%d)", source, externalTrialsPerEvent.size())
    }

    override fun contents(): Table {
        val colWidth = width / 5
        val table = Tables.createFixedWidthCols(colWidth, colWidth*2, colWidth, colWidth)
        table.addHeaderCell(Cells.createHeader("Event"))
        table.addHeaderCell(Cells.createHeader("Trial title"))
        table.addHeaderCell(Cells.createHeader("NCT id"))
        table.addHeaderCell(Cells.createHeader("Potentially recruiting countries"))

        externalTrialsPerEvent.keySet().sorted().distinct().forEach { event ->
            table.addCell(Cells.createContent(event))
            val trialList = externalTrialsPerEvent[event].joinToString(COMMA_SEPARATOR)
            table.addCell(Cells.createContent(trialList))
            table.addCell(Cells.createContent("nct id"))
            table.addCell(Cells.createContent("countries"))
        }
        return makeWrapping(table)
    }
}