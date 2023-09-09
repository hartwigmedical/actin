package com.hartwig.actin.report.pdf.tables.treatment

import com.google.common.collect.Multimap
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.COMMA_SEPARATOR
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleExternalTrialsGenerator(
    private val source: String, private val externalTrialsPerEvent: Multimap<String, String>,
    private val keyWidth: Float, private val valueWidth: Float
) : TableGenerator {
    override fun title(): String {
        return String.format("%s trials potentially eligible based on molecular results (%d)", source, externalTrialsPerEvent.size())
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addHeaderCell(Cells.createHeader("Event"))
        table.addHeaderCell(Cells.createHeader("Trials"))

        externalTrialsPerEvent.keySet().sorted().distinct().forEach { event ->
            table.addCell(Cells.createContent(event))
            val trialList = externalTrialsPerEvent[event].joinToString(COMMA_SEPARATOR)
            table.addCell(Cells.createContent(trialList))
        }
        return makeWrapping(table)
    }
}