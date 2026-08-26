package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.clinical.sort.SurgeryDescendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.HeartMeasurement
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.DateFunctions.toDateString
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
    private val referenceDate: LocalDate,
    private val labels: ReportLabels
) : TableGenerator {

    override fun title(): String {
        return labels.clinicalDetails.patientDetailsTitle(date(record.patient.questionnaireDate))
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey(labels.clinicalDetails.keyToxicities()))
        table.addCell(Cells.createValue(toxicities(record)))
        val infectionStatus = record.clinicalStatus.infectionStatus
        if (infectionStatus != null && infectionStatus.hasActiveInfection) {
            table.addCell(Cells.createKey(labels.clinicalDetails.keyInfection()))
            val description = infectionStatus.description
            table.addCell(Cells.createValue(description ?: labels.clinicalDetails.valueInfectionUnknown()))
        }
        record.ecgs.firstOrNull()?.let { ecg ->
            table.addCell(Cells.createKey(labels.clinicalDetails.keyEcg()))
            val aberration = ecg.name
            val description = aberration ?: labels.clinicalDetails.valueEcgUnknown()
            table.addCell(Cells.createValue(description))

            if (ecg.measurementType == HeartMeasurementType.QTCF) {
                createMeasureCells(table, labels.clinicalDetails.keyQtcf(), ecg)
            }

            if (ecg.measurementType == HeartMeasurementType.JTC) {
                createMeasureCells(table, labels.clinicalDetails.keyJtc(), ecg)
            }
        }
        if (record.clinicalStatus.lvef != null) {
            table.addCell(Cells.createKey(labels.clinicalDetails.keyLvef()))
            table.addCell(Cells.createValue(Formats.percentage(record.clinicalStatus.lvef!!)))
        }
        table.addCell(Cells.createKey(labels.clinicalDetails.keyAllergies()))
        table.addCell(Cells.createValue(allergies(record.intolerances)))
        if (record.surgeries.isNotEmpty()) {
            table.addCell(Cells.createKey(labels.clinicalDetails.keySurgeries()))
            table.addCell(Cells.createValue(surgeries(record.surgeries)))
        }
        return table
    }

    private fun createMeasureCells(table: Table, key: String, measure: HeartMeasurement) {
        table.addCell(Cells.createKey(key))
        table.addCell(Cells.createValue(Formats.twoDigitNumber(measure.value!!)).toString() + " " + measure.unit)
    }

    private fun toxicities(record: PatientRecord): String {
        val (questionnaireToxicities, ehrToxicities) = ToxicityFunctions.selectRelevantToxicities(record, referenceDate)
            .filter { it.grade == null || it.grade!! >= 2 }
            .partition { it.source == ToxicitySource.QUESTIONNAIRE }

        val questionnaireSummary = when {
            questionnaireToxicities.isEmpty() -> null
            questionnaireToxicities.all { it.name.isNullOrEmpty() } -> "Yes (details unknown)"
            else -> formatToxicities(questionnaireToxicities)
        }

        val ehrSummary = if (ehrToxicities.isEmpty()) null else formatToxicities(ehrToxicities)

        return Formats.valueOrDefault(listOfNotNull(questionnaireSummary, ehrSummary).joinToString("; "), "None")
    }

    private fun formatToxicities(toxicities: List<Toxicity>) =
        toxicities.map {
            val name = (it.name ?: "Unknown")
            val grade =
                (it.grade?.let { grade -> "GR $grade" } ?: if (it.source != ToxicitySource.QUESTIONNAIRE) "unknown grade" else "")
            val date = toDateString(it.year, it.month, it.day) ?: "unknown date"
            "$name ($grade${if (grade.isNotEmpty()) ", " else ""}$date)"
        }.distinct().joinToString(Formats.COMMA_SEPARATOR)

    private fun allergies(intolerances: List<Intolerance>): String {
        val intoleranceSummary = intolerances.filter { !it.name.equals("none", ignoreCase = true) }
            .joinToString(Formats.COMMA_SEPARATOR) { it.display() }
        return Formats.valueOrDefault(intoleranceSummary, "None")
    }

    private fun surgeries(surgeries: List<Surgery>): String {
        val recentSurgeries = surgeries.filter { it.endDate?.let { endDate -> endDate >= referenceDate.minusMonths(2) } ?: true }
        return Formats.valueOrDefault(
            recentSurgeries.sortedWith(SurgeryDescendingDateComparator())
                .joinToString(Formats.COMMA_SEPARATOR) { date(it.endDate) + if (it.name?.isNotEmpty() == true) " ${it.name}" else "" },
            "None"
        )
    }
}
