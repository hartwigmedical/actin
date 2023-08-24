package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ECGMeasure
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

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
        if (record.surgeries().isNotEmpty()) {
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
            val toxicitySummary = record.toxicities().filter { toxicity ->
                val grade = toxicity.grade()
                grade != null && grade >= 2 || toxicity.source() == ToxicitySource.QUESTIONNAIRE
            }.joinToString(Formats.COMMA_SEPARATOR) { toxicity ->
                toxicity.name() + toxicity.grade()?.let { " ($it)" }
            }
            return Formats.valueOrDefault(toxicitySummary, "None")
        }

        private fun complications(record: ClinicalRecord): String {
            val complications = record.complications()
            val hasComplications = record.clinicalStatus().hasComplications() == true
            if (complications == null) {
                return if (hasComplications) "Yes (complication details unknown)" else "Unknown"
            }
            val complicationSummary = complications.joinToString(Formats.COMMA_SEPARATOR) { complication ->
                complication.name() + toDateString(complication.year(), complication.month())?.let { " ($it)" }
            }
            return Formats.valueOrDefault(complicationSummary, if (hasComplications) "Yes (complication details unknown)" else "None")
        }

        private fun toDateString(maybeYear: Int?, maybeMonth: Int?): String? {
            return maybeYear?.let { year ->
                maybeMonth?.let { month -> "$month/$year" } ?: year.toString()
            }
        }

        private fun allergies(intolerances: List<Intolerance>): String {
            val intoleranceSummary = intolerances.filter { !it.name().equals("none", ignoreCase = true) }
                .joinToString(Formats.COMMA_SEPARATOR) {
                    it.name() + if (it.category().isNotEmpty()) " (${it.category()})" else ""
                }
            return Formats.valueOrDefault(intoleranceSummary, "None")
        }

        private fun surgeries(surgeries: List<Surgery>): String {
            return Formats.valueOrDefault(surgeries.joinToString(Formats.COMMA_SEPARATOR) { date(it.endDate()) }, "None")
        }
    }
}