package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.interpretation.IhcTestInterpretation
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
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
        if (ihcTests.isEmpty()) {
            table.addCell(Cells.createSpanningValue("None", table).addStyle(Styles.tableKeyStyle()))
        } else {
            interpreter.interpret(ihcTests).forEach { ihcTestInterpretation ->
                ihcTestInterpretationContents(ihcTestInterpretation, table)
            }
        }
        return table
    }

    private fun ihcTestInterpretationContents(ihcTestInterpretation: IhcTestInterpretation, table: Table) {
        ihcTestInterpretation.results
            .sortedWith(compareBy({ it.sortPrecedence }, { it.grouping }))
            .groupBy { it.grouping }
            .forEach { group ->
                table.addCell(Cells.createKey(group.key))
                val paragraphs = group.value
                    .groupBy { it.date }.entries
                    .sortedWith(nullsLast(compareByDescending { it.key }))
                    .map { sameDateGroup ->
                        Paragraph(sameDateGroup.value.joinToString { it.details } + (sameDateGroup.key?.let { " ($it)" } ?: ""))
                    }
                table.addCell(Cells.createValue(paragraphs))
            }
    }
}