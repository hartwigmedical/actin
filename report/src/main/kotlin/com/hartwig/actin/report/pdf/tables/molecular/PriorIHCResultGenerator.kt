package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.report.interpretation.PriorIHCTestInterpreter
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class PriorIHCResultGenerator(
    private val patientRecord: PatientRecord,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val interpreter: PriorIHCTestInterpreter
) {
    fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        if (patientRecord.priorIHCTests.isEmpty()) {
            table.addCell(Cells.createValue("No prior IHC molecular tests"))
        } else {
            val sortedInterpretation = interpreter.interpret(patientRecord)
            for (priorMolecularTestInterpretation in sortedInterpretation) {
                table.addCell(Cells.createSubTitle(priorMolecularTestInterpretation.type + " results"))
                table.addCell(Cells.createValue(priorMolecularTestInterpretation.results.sortedBy { it.sortPrecedence }
                    .groupBy { it.grouping }
                    .map { Paragraph("${it.key}: ${it.value.joinToString { i -> i.details }}") }))
            }
        }
        return table
    }
}