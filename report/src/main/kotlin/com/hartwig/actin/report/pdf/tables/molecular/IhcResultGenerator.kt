package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.interpretation.IhcTestInterpretation
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
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
        ihcTestInterpretation.results.sortedBy { it.sortPrecedence }
            .groupBy { it.grouping }
            .forEach {
                table.addCell(Cells.createKey(it.key))
                table.addCell(
                    Cells.createValue(it.value.joinToString { i ->
                        i.date?.let { d -> "${i.details} (${date(d)})" } ?: i.details
                    })
                )
            }
    }
}