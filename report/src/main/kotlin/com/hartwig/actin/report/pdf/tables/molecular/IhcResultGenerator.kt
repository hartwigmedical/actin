package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.interpretation.IhcTestInterpretation
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class IhcResultGenerator(
    private val ihcTests: List<IhcTest>,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val interpreter: IhcTestInterpreter
) : TableGenerator {

    override fun title(): String {
        return "IHC results"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        if (ihcTests.isNotEmpty()) {
            interpreter.interpret(ihcTests).forEach { ihcTestInterpretation -> ihcTestInterpretationContents(ihcTestInterpretation, table) }
        }
        return table
    }

    private fun ihcTestInterpretationContents(ihcTestInterpretation: IhcTestInterpretation, table: Table) {
        ihcTestInterpretation.results
            .sortedWith(compareBy({ it.sortPrecedence }, { it.grouping }))
            .groupBy { it.grouping }
            .forEach { (group, results) ->
                table.addCell(Cells.createKey(group))
                val paragraphs = results
                    .groupBy { it.date }.entries
                    .sortedWith(nullsLast(compareByDescending { it.key }))
                    .map { (date, resultsForDate) ->
                        Paragraph(resultsForDate.joinToString { it.details } + (date?.let { " ($it)" } ?: ""))
                    }
                table.addCell(Cells.createValue(paragraphs))
            }
    }
}