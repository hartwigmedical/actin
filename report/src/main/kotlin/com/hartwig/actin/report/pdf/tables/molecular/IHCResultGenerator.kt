package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.report.interpretation.IHCTestInterpreter
import com.hartwig.actin.report.interpretation.MolecularTestInterpretation
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class IHCResultGenerator(
    private val ihcTests: List<IHCTest>,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val interpreter: IHCTestInterpreter
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
            interpreter.interpret(ihcTests).forEach { molecularTestInterpretation ->
                molecularTestInterpretationContents(molecularTestInterpretation, table)
            }
        }
        return table
    }

    private fun molecularTestInterpretationContents(molecularTestInterpretation: MolecularTestInterpretation, table: Table) {
        molecularTestInterpretation.results.sortedBy { it.sortPrecedence }
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