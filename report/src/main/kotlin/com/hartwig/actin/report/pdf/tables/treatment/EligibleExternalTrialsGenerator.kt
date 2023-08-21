package com.hartwig.actin.report.pdf.tables.treatment

import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
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
        val events: Set<String> = Sets.newTreeSet(
            externalTrialsPerEvent.keySet()
        )
        for (event in events) {
            table.addCell(Cells.createContent(event))
            val joiner = Formats.commaJoiner()
            for (trial in externalTrialsPerEvent[event]) {
                joiner.add(trial)
            }
            table.addCell(Cells.createContent(joiner.toString()))
        }
        return makeWrapping(table)
    }
}