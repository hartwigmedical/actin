package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpretation
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
        table.addCell(Cells.createSubTitle("IHC results"))
        val interpretation = interpreter.interpret(molecularHistory.molecularTests)
        if (molecularHistory.molecularTests.isEmpty()) {
            table.addCell(Cells.createValue("None"))
        } else {
            val paragraphs = generatePriorTestParagraphs(interpretation)
            table.addCell(Cells.createValue(paragraphs))
        }
        return table
    }

    companion object {
        private fun generatePriorTestParagraphs(interpretation: PriorMolecularTestInterpretation): List<Paragraph> {
            return interpretation.displaySections
                .map {
                    Paragraph("${it.type} ${it.results}")
                }
        }
    }
}