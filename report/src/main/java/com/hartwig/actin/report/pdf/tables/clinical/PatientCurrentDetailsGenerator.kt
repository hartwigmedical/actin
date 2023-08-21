package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ECGMeasure
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.util.Strings
import java.lang.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.String

class PatientCurrentDetailsGenerator(private val record: ClinicalRecord, private val keyWidth: Float, private val valueWidth: Float) :
    TableGenerator {
    override fun title(): String {
        return "Patient current details (" + date(record.patient().questionnaireDate()) + ")"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("Unresolved toxicities grade => 2"))
        table.addCell(Cells.createValue(unresolvedToxicities(record)))
        val infectionStatus = record.clinicalStatus().infectionStatus()
        if (infectionStatus != null && infectionStatus.hasActiveInfection()) {
            table.addCell(Cells.createKey("Significant infection"))
            val description = infectionStatus.description()
            table.addCell(Cells.createValue(description ?: "Yes (infection details unknown)"))
        }
        val ecg = record.clinicalStatus().ecg()
        if (ecg != null && ecg.hasSigAberrationLatestECG()) {
            if (ecg.hasSigAberrationLatestECG()) {
                table.addCell(Cells.createKey("Significant aberration on latest ECG"))
                val aberration = ecg.aberrationDescription()
                val description = aberration ?: "Yes (ECG aberration details unknown)"
                table.addCell(Cells.createValue(description))
            }
            val qtcfMeasure = ecg.qtcfMeasure()
            if (qtcfMeasure != null) {
                createMeasureCells(table, "QTcF", qtcfMeasure)
            }
            val jtcMeasure = ecg.jtcMeasure()
            if (jtcMeasure != null) {
                createMeasureCells(table, "JTc", jtcMeasure)
            }
        }
        if (record.clinicalStatus().lvef() != null) {
            table.addCell(Cells.createKey("LVEF"))
            table.addCell(Cells.createValue(Formats.percentage(record.clinicalStatus().lvef()!!)))
        }
        table.addCell(Cells.createKey("Cancer-related complications"))
        table.addCell(Cells.createValue(complications(record)))
        table.addCell(Cells.createKey("Known allergies"))
        table.addCell(Cells.createValue(allergies(record.intolerances())))
        if (!record.surgeries().isEmpty()) {
            table.addCell(Cells.createKey("Recent surgeries"))
            table.addCell(Cells.createValue(surgeries(record.surgeries())))
        }
        return table
    }

    companion object {
        private fun createMeasureCells(table: Table, key: String, measure: ECGMeasure) {
            table.addCell(Cells.createKey(key))
            table.addCell(Cells.createValue(Formats.twoDigitNumber(measure.value().toDouble())).toString() + " " + measure.unit())
        }

        //TODO: For source EHR, only consider the most recent value of each toxicity and write these with "From EHR: " for clarity
        private fun unresolvedToxicities(record: ClinicalRecord): String {
            val joiner = Formats.commaJoiner()
            for (toxicity in record.toxicities()) {
                val grade = toxicity.grade()
                if (grade != null && grade >= 2 || toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                    val gradeString = if (grade != null) " ($grade)" else Strings.EMPTY
                    joiner.add(toxicity.name() + gradeString)
                }
            }
            return Formats.valueOrDefault(joiner.toString(), "None")
        }

        private fun complications(record: ClinicalRecord): String {
            val complications = record.complications()
            val hasComplications = Boolean.TRUE == record.clinicalStatus().hasComplications()
            if (complications == null) {
                return if (hasComplications) "Yes (complication details unknown)" else "Unknown"
            }
            val joiner = Formats.commaJoiner()
            for (complication in complications) {
                val date = toDateString(complication.year(), complication.month())
                var dateAddition = Strings.EMPTY
                if (date != null) {
                    dateAddition = " ($date)"
                }
                joiner.add(complication.name() + dateAddition)
            }
            return Formats.valueOrDefault(joiner.toString(), if (hasComplications) "Yes (complication details unknown)" else "None")
        }

        private fun toDateString(year: Int?, month: Int?): String? {
            return if (year != null) {
                if (month != null) "$month/$year" else year.toString()
            } else {
                null
            }
        }

        private fun allergies(intolerances: List<Intolerance>): String {
            val joiner = Formats.commaJoiner()
            for (intolerance in intolerances) {
                if (!intolerance.name().equals("none", ignoreCase = true)) {
                    val addition = if (!intolerance.category().isEmpty()) " (" + intolerance.category() + ")" else Strings.EMPTY
                    joiner.add(intolerance.name() + addition)
                }
            }
            return Formats.valueOrDefault(joiner.toString(), "None")
        }

        private fun surgeries(surgeries: List<Surgery>): String {
            val joiner = Formats.commaJoiner()
            for (surgery in surgeries) {
                joiner.add(date(surgery.endDate()))
            }
            return Formats.valueOrDefault(joiner.toString(), "None")
        }
    }
}