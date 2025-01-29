package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.sort.SurgeryDescendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.ECGMeasure
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import java.time.LocalDate

class PatientCurrentDetailsGenerator(
    private val record: PatientRecord,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val referenceDate: LocalDate
) : TableGenerator {

    override fun title(): String {
        return "Patient current details (" + date(record.patient.questionnaireDate) + ")"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("Unresolved toxicities grade => 2"))
        table.addCell(Cells.createValue(unresolvedToxicities(record)))
        val infectionStatus = record.clinicalStatus.infectionStatus
        if (infectionStatus != null && infectionStatus.hasActiveInfection) {
            table.addCell(Cells.createKey("Significant infection"))
            val description = infectionStatus.description
            table.addCell(Cells.createValue(description ?: "Yes (infection details unknown)"))
        }
        val ecg = record.clinicalStatus.ecg
        if (ecg != null && ecg.hasSigAberrationLatestECG) {
            table.addCell(Cells.createKey("Significant aberration on latest ECG"))
            val aberration = ecg.aberrationDescription
            val description = aberration ?: "Yes (ECG aberration details unknown)"
            table.addCell(Cells.createValue(description))

            val qtcfMeasure = ecg.qtcfMeasure
            if (qtcfMeasure != null) {
                createMeasureCells(table, "QTcF", qtcfMeasure)
            }
            val jtcMeasure = ecg.jtcMeasure
            if (jtcMeasure != null) {
                createMeasureCells(table, "JTc", jtcMeasure)
            }
        }
        if (record.clinicalStatus.lvef != null) {
            table.addCell(Cells.createKey("LVEF"))
            table.addCell(Cells.createValue(Formats.percentage(record.clinicalStatus.lvef!!)))
        }
        table.addCell(Cells.createKey("Cancer-related complications"))
        table.addCell(Cells.createValue(complications(record)))
        table.addCell(Cells.createKey("Known allergies"))
        table.addCell(Cells.createValue(allergies(record.intolerances)))
        if (record.surgeries.isNotEmpty()) {
            table.addCell(Cells.createKey("Recent surgeries"))
            table.addCell(Cells.createValue(surgeries(record.surgeries)))
        }
        return table
    }

    private fun createMeasureCells(table: Table, key: String, measure: ECGMeasure) {
        table.addCell(Cells.createKey(key))
        table.addCell(Cells.createValue(Formats.twoDigitNumber(measure.value!!.toDouble())).toString() + " " + measure.unit)
    }

    private fun unresolvedToxicities(record: PatientRecord): String {
        val (questionnaireToxicities, ehrToxicities) = record.toxicities
            .filter { it.endDate?.let { endDate -> endDate >= referenceDate } ?: true }
            .partition { it.source == ToxicitySource.QUESTIONNAIRE }

        val questionnaireSummary = if (questionnaireToxicities.isEmpty()) null else {
            formatToxicities(questionnaireToxicities).ifEmpty { "Yes (details unknown)" }
        }
        val ehrSummary = filterUncuratedToxicities(ehrToxicities).let { filteredEHRToxicities ->
            if (filteredEHRToxicities.isEmpty()) null else "From EHR: " + formatToxicities(filteredEHRToxicities)
        }
        return Formats.valueOrDefault(listOfNotNull(questionnaireSummary, ehrSummary).joinToString("; "), "None")
    }

    private fun formatToxicities(filteredToxicities: List<Toxicity>) =
        filteredToxicities.map { (it.name ?: "Unknown") + (it.grade?.let { grade -> " ($grade)" } ?: "") }.distinct()
            .joinToString(Formats.COMMA_SEPARATOR)

    private fun filterUncuratedToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        return toxicities.filter { (it.grade ?: -1) >= 2 }
            .groupBy(Toxicity::name)
            .map { (_, toxicitiesWithName) -> toxicitiesWithName.maxBy(Toxicity::evaluatedDate) }
    }

    private fun complications(record: PatientRecord): String {
        val complicationSummary = record.complications.filter { it.name != null }.joinToString(Formats.COMMA_SEPARATOR) { complication ->
            complication.name + (toDateString(complication.year, complication.month)?.let { " ($it)" } ?: "")
        }
        val defaultValue = when (record.clinicalStatus.hasComplications) {
            true -> "Yes (complication details unknown)"
            false -> "None"
            else -> "Unknown"
        }
        return Formats.valueOrDefault(complicationSummary, defaultValue)
    }

    private fun toDateString(maybeYear: Int?, maybeMonth: Int?): String? {
        return maybeYear?.let { year ->
            maybeMonth?.let { month -> "$month/$year" } ?: year.toString()
        }
    }

    private fun allergies(intolerances: List<Intolerance>): String {
        val intoleranceSummary = intolerances.filter { !it.name.equals("none", ignoreCase = true) }
            .joinToString(Formats.COMMA_SEPARATOR) { it.display() }
        return Formats.valueOrDefault(intoleranceSummary, "None")
    }

    private fun surgeries(surgeries: List<Surgery>): String {
        return Formats.valueOrDefault(
            surgeries.sortedWith(SurgeryDescendingDateComparator())
                .joinToString(Formats.COMMA_SEPARATOR) { date(it.endDate) + if (it.name?.isNotEmpty() == true) " ${it.name}" else "" },
            "None"
        )
    }
}