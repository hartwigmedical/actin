package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class TumorDetailsGenerator(
    private val record: PatientRecord,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val labels: ReportLabels
) : TableGenerator {

    override fun title(): String {
        return labels.clinicalDetails.tumorDetailsTitle()
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey(labels.clinicalDetails.keyMeasurableDisease()))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.tumor.hasMeasurableDisease)))
        table.createLesionDetails()
        return table
    }

    private fun Table.createLesionDetails() {
        val lesions = TumorDetailsInterpreter.classifyLesions(record.tumor)

        with(lesions) {
            createLesionRow(labels.clinicalDetails.keyLesions(), nonLymphNodeLesions + lymphNodeLesions + suspectedLesions)
            if (negativeCategories.isNotEmpty()) createLesionRow(labels.clinicalDetails.keyNoLesions(), negativeCategories)
        }
    }

    private fun Table.createLesionRow(key: String, value: List<String>) {
        addCell(Cells.createKey(key))
        addCell(Cells.createValue(value.joinToString().ifEmpty { Formats.VALUE_NONE }))
    }
}
