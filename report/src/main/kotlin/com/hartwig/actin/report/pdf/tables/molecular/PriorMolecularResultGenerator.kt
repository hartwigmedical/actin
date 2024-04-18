package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class PriorMolecularResultGenerator(
    private val molecularHistory: MolecularHistory,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val interpreter: PriorMolecularTestInterpreter
) {
    fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        if (molecularHistory.molecularTests.isEmpty()) {
            table.addCell(Cells.createValue("None"))
        } else {
            val interpretation = interpreter.interpret(molecularHistory.molecularTests)
            for (priorMolecularTestInterpretation in interpretation) {
                table.addCell(Cells.createSubTitle(priorMolecularTestInterpretation.type + " Results"))
                table.addCell(Cells.createValue(priorMolecularTestInterpretation.results.groupBy { it.grouping }
                    .map { Paragraph("${it.key}: ${it.value.joinToString { i -> i.details }}") }))
            }
        }
        return table
    }
}