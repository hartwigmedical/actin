package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class TumorDetailsGenerator(private val record: PatientRecord, private val keyWidth: Float, private val valueWidth: Float) :
    TableGenerator {

    override fun title(): String {
        return "Tumor details (${date(record.patient.questionnaireDate)})"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("Measurable disease"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.tumor.hasMeasurableDisease)))
        table.createLesionDetails()
        return table
    }

    private fun Table.createLesionDetails() {
        val lesions = TumorDetailsInterpreter.classifyLesions(record.tumor)

        with(lesions) {
            val negative = suspectedCategorizedLesions + suspectedCategorizedLesions + negativeCategories

            createLesionRow("Known lesions", nonLymphNodeLesions + lymphNodeLesions)
            createLesionRow("Unknown lesions", unknownCategories)
            if (negative.isNotEmpty()) createLesionRow("No lesions present", negative)
        }
    }

    private fun Table.createLesionRow(key: String, value: List<String>) {
        addCell(Cells.createKey(key))
        addCell(Cells.createValue(value.joinToString().ifEmpty { Formats.VALUE_NONE }))
    }
}