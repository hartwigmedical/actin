package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.report.interpretation.IHCTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class IHCResultGenerator(
    private val patientRecord: PatientRecord,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val interpreter: IHCTestInterpreter
) : TableGenerator {

    override fun title(): String {
        return "Prior IHC results"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        if (patientRecord.ihcTests.isEmpty()) {
            table.addCell(Cells.createSpanningValue("No prior IHC molecular tests", table))
        } else {
            val sortedInterpretation = interpreter.interpret(patientRecord)
            for (molecularTestInterpretation in sortedInterpretation) {
                table.addCell(Cells.createSubTitle(molecularTestInterpretation.type + " results"))
                table.addCell(Cells.createValue(molecularTestInterpretation.results.sortedBy { it.sortPrecedence }
                    .groupBy { it.grouping }
                    .map {
                        Paragraph(
                            "${it.key}: ${
                                it.value.joinToString { i ->
                                    i.date?.let { d -> "${i.details} ($d)" }
                                        ?: i.details
                                }
                            }"
                        )
                    }))
            }
        }
        return table
    }
}