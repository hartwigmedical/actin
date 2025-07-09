package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class TumorDetailsGenerator(private val report: Report, private val keyWidth: Float, private val valueWidth: Float) :
    TableGenerator {

    override fun title(): String {
        val date = record.patient.questionnaireDate?.let { record.patient.registrationDate }
        return "Tumor details (${date(date)})"
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

    private val record = report.patientRecord

    private fun Table.createLesionDetails() {
        with (record.tumor) {
            createLesionRow("CNS", activeLesionString(hasCnsLesions, hasActiveCnsLesions))
            createLesionRow("Brain", activeLesionString(hasBrainLesions, hasActiveBrainLesions))

            if (!report.config.includeLesionsInTumorSummary) {
                createLesionRow("Liver", Formats.yesNoUnknown(hasLiverLesions))
                createLesionRow("Bone", Formats.yesNoUnknown(hasBoneLesions))
            }
        }
    }

    private fun Table.createLesionRow(key: String, value: String) {
        addCell(Cells.createKey("$key lesions present"))
        addCell(Cells.createValue(value))
    }

    private fun activeLesionString(hasLesions: Boolean?, active: Boolean?): String {
        val activeString =
            if (hasLesions == true) active?.let { if (it) " (active)" else " (not active)" } ?: " (unknown if active)" else null
        return Formats.yesNoUnknown(hasLesions) + activeString.orEmpty()
    }
}