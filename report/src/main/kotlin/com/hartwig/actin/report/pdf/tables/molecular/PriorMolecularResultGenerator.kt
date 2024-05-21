package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.ExperimentType
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
            val sortedInterpretation = interpreter.interpret(molecularHistory).sortedBy {
                when (it.type) {
                    ExperimentType.IHC.display() -> 0
                    ExperimentType.ARCHER.display() -> 1
                    ExperimentType.GENERIC_PANEL.display() -> 2
                    ExperimentType.OTHER.display() -> 3
                    else -> 4
                }
            }
            for (priorMolecularTestInterpretation in sortedInterpretation) {
                table.addCell(Cells.createSubTitle(priorMolecularTestInterpretation.type + " Results"))
                table.addCell(Cells.createValue(priorMolecularTestInterpretation.results.sortedBy { it.sortPrecedence }
                    .groupBy { it.grouping }
                    .map { Paragraph("${it.key}: ${it.value.joinToString { i -> i.details }}") }))
            }
        }
        return table
    }
}