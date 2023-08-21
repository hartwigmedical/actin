package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpretation
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.util.Strings
import java.lang.Boolean
import java.time.LocalDate
import java.util.stream.Collectors
import kotlin.Comparator
import kotlin.Float
import kotlin.FloatArray
import kotlin.String

class LabResultsGenerator private constructor(
    private val labInterpretation: LabInterpretation, private val key1Width: Float, private val key2Width: Float,
    private val key3Width: Float, private val valueWidth: Float
) : TableGenerator {
    override fun title(): String {
        return "Laboratory results"
    }

    override fun contents(): Table {
        val dates = labInterpretation.allDates()
            .stream()
            .sorted(Comparator.reverseOrder())
            .distinct()
            .limit(MAX_LAB_DATES.toLong())
            .sorted()
            .collect(Collectors.toList())
        val table = Tables.createFixedWidthCols(*defineWidths())
        table.addHeaderCell(Cells.createHeader(Strings.EMPTY))
        table.addHeaderCell(Cells.createHeader(Strings.EMPTY))
        table.addHeaderCell(Cells.createHeader(Strings.EMPTY))
        for (date in dates) {
            table.addHeaderCell(Cells.createHeader(date(date)))
        }
        for (i in dates.size until MAX_LAB_DATES) {
            table.addHeaderCell(Cells.createHeader(Strings.EMPTY))
        }
        table.addCell(Cells.createKey("Liver function"))
        table.addCell(Cells.createKey("Total bilirubin"))
        addLabMeasurements(table, dates, LabMeasurement.TOTAL_BILIRUBIN)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("ASAT"))
        addLabMeasurements(table, dates, LabMeasurement.ASPARTATE_AMINOTRANSFERASE)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("ALAT"))
        addLabMeasurements(table, dates, LabMeasurement.ALANINE_AMINOTRANSFERASE)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("ALP"))
        addLabMeasurements(table, dates, LabMeasurement.ALKALINE_PHOSPHATASE)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Albumin"))
        addLabMeasurements(table, dates, LabMeasurement.ALBUMIN)
        table.addCell(Cells.createKey("Kidney function"))
        table.addCell(Cells.createKey("Creatinine"))
        addLabMeasurements(table, dates, LabMeasurement.CREATININE)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("CKD-EPI eGFR"))
        addLabMeasurements(table, dates, LabMeasurement.EGFR_CKD_EPI)
        table.addCell(Cells.createKey("Other"))
        table.addCell(Cells.createKey("Hemoglobin"))
        addLabMeasurements(table, dates, LabMeasurement.HEMOGLOBIN)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Thrombocytes"))
        addLabMeasurements(table, dates, LabMeasurement.THROMBOCYTES_ABS)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("LDH"))
        addLabMeasurements(table, dates, LabMeasurement.LACTATE_DEHYDROGENASE)
        table.addCell(Cells.createKey("Tumor markers"))
        table.addCell(Cells.createKey("CA 15.3"))
        addLabMeasurements(table, dates, LabMeasurement.CA_153)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("CA 125"))
        addLabMeasurements(table, dates, LabMeasurement.CA_125)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("CA 19.9"))
        addLabMeasurements(table, dates, LabMeasurement.CA_199)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("CEA"))
        addLabMeasurements(table, dates, LabMeasurement.CEA)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("PSA"))
        addLabMeasurements(table, dates, LabMeasurement.PSA)
        if (labInterpretation.allDates().size > MAX_LAB_DATES) {
            val note = "Note: Only the most recent " + MAX_LAB_DATES + " lab results have been displayed"
            table.addCell(Cells.createSpanningSubNote(note, table))
        }
        return table
    }

    private fun defineWidths(): FloatArray {
        val widths = FloatArray(3 + MAX_LAB_DATES)
        widths[0] = key1Width
        widths[1] = key2Width
        widths[2] = key3Width
        for (i in 0 until MAX_LAB_DATES) {
            widths[3 + i] = valueWidth / MAX_LAB_DATES
        }
        return widths
    }

    private fun addLabMeasurements(table: Table, dates: List<LocalDate>, measurement: LabMeasurement) {
        table.addCell(Cells.createKey(buildLimitString(labInterpretation.mostRecentValue(measurement))))
        for (date in dates) {
            val joiner = Formats.commaJoiner()
            var style = Styles.tableHighlightStyle()
            for (lab in labInterpretation.valuesOnDate(measurement, date)!!) {
                var value = Formats.twoDigitNumber(lab.value()) + " " + lab.unit().display()
                if (!lab.comparator().isEmpty()) {
                    value = lab.comparator() + " " + value
                }
                joiner.add(value)
                if (lab.isOutsideRef != null && Boolean.TRUE == lab.isOutsideRef) {
                    style = Styles.tableWarnStyle()
                }
            }
            table.addCell(Cells.create(Paragraph(joiner.toString()).addStyle(style)))
        }
        for (i in dates.size until MAX_LAB_DATES) {
            table.addCell(Cells.createEmpty())
        }
    }

    companion object {
        private const val MAX_LAB_DATES = 5
        fun fromRecord(record: ClinicalRecord, keyWidth: Float, valueWidth: Float): LabResultsGenerator {
            val key1Width = keyWidth / 3
            val key2Width = keyWidth / 3
            val key3Width = keyWidth - key1Width - key2Width
            return LabResultsGenerator(LabInterpreter.interpret(record.labValues()), key1Width, key2Width, key3Width, valueWidth)
        }

        private fun buildLimitString(lab: LabValue?): String {
            if (lab == null) {
                return Strings.EMPTY
            }
            val refLimitLow = lab.refLimitLow()
            val refLimitUp = lab.refLimitUp()
            if (refLimitLow == null && refLimitUp == null) {
                return Strings.EMPTY
            }
            val limit: String
            limit = if (refLimitLow == null) {
                "< " + Formats.twoDigitNumber(refLimitUp!!)
            } else if (refLimitUp == null) {
                "> " + Formats.twoDigitNumber(refLimitLow)
            } else {
                Formats.twoDigitNumber(refLimitLow) + " - " + Formats.twoDigitNumber(refLimitUp)
            }
            return "(" + limit + " " + lab.unit().display() + ")"
        }
    }
}