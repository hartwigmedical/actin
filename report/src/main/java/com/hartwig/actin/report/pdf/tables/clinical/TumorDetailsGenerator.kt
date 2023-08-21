package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.util.Strings

class TumorDetailsGenerator(private val record: ClinicalRecord, private val keyWidth: Float, private val valueWidth: Float) :
    TableGenerator {
    override fun title(): String {
        return "Tumor details (" + date(record.patient().questionnaireDate()) + ")"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("Measurable disease"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.tumor().hasMeasurableDisease())))
        table.addCell(Cells.createKey("CNS lesion status"))
        table.addCell(Cells.createValue(cnsLesions(record.tumor())))
        table.addCell(Cells.createKey("Brain lesion status"))
        table.addCell(Cells.createValue(brainLesions(record.tumor())))
        return table
    }

    companion object {
        private fun cnsLesions(tumor: TumorDetails): String {
            if (tumor.hasCnsLesions() == null) {
                return Formats.VALUE_UNKNOWN
            }
            return if (tumor.hasCnsLesions()!!) {
                activeLesionString("Present CNS lesions", tumor.hasActiveCnsLesions())
            } else {
                "No known CNS lesions"
            }
        }

        private fun brainLesions(tumor: TumorDetails): String {
            if (tumor.hasBrainLesions() == null) {
                return Formats.VALUE_UNKNOWN
            }
            return if (tumor.hasBrainLesions()!!) {
                activeLesionString("Present brain lesions", tumor.hasActiveBrainLesions())
            } else {
                "No known brain lesions"
            }
        }

        private fun activeLesionString(type: String, active: Boolean?): String {
            var activeString = Strings.EMPTY
            if (active != null) {
                activeString = if (active) " (active)" else " (not active)"
            }
            return type + activeString
        }
    }
}