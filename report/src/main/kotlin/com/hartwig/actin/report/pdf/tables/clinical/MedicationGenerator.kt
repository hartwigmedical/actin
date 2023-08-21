package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.util.Strings
import java.util.*

class MedicationGenerator(private val medications: List<Medication>, private val totalWidth: Float) : TableGenerator {
    override fun title(): String {
        return "Active medication details"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(1f, 1f, 1f, 1f, 1f, 1f).setWidth(
            totalWidth
        )
        table.addHeaderCell(Cells.createHeader("Medication"))
        table.addHeaderCell(Cells.createHeader("Administration route"))
        table.addHeaderCell(Cells.createHeader("Start date"))
        table.addHeaderCell(Cells.createHeader("Stop date"))
        table.addHeaderCell(Cells.createHeader("Dosage"))
        table.addHeaderCell(Cells.createHeader("Frequency"))
        medications.stream()
            .distinct()
            .filter { medication: Medication ->
                Optional.ofNullable(medication.status())
                    .map { status: MedicationStatus -> status.display() == "Active" || status.display() == "Planned" }
                    .orElse(false)
            }
            .forEach { medication: Medication ->
                table.addCell(Cells.createContent(medication.name()))
                table.addCell(Cells.createContent(administrationRoute(medication)))
                table.addCell(Cells.createContent(Formats.date(medication.startDate(), Strings.EMPTY)))
                table.addCell(Cells.createContent(Formats.date(medication.stopDate(), Strings.EMPTY)))
                table.addCell(Cells.createContent(dosage(medication)))
                table.addCell(Cells.createContent(frequency(medication.dosage())))
            }
        return makeWrapping(table)
    }

    companion object {
        private const val SPECIFIC_OR_UNKNOWN = "specific prescription|unknown prescription"
        private fun administrationRoute(medication: Medication): String {
            return if (medication.administrationRoute() != null) medication.administrationRoute()!! else ""
        }

        private fun dosage(medication: Medication): String {
            val dosage = medication.dosage()
            val dosageMin =
                if (dosage.dosageMin() != null && dosage.dosageMin() != 0.0) Formats.twoDigitNumber(dosage.dosageMin()!!) else "?"
            val dosageMax =
                if (dosage.dosageMax() != null && dosage.dosageMax() != 0.0) Formats.twoDigitNumber(dosage.dosageMax()!!) else "?"
            var result = if (dosageMin == dosageMax) dosageMin else "$dosageMin - $dosageMax"
            val ifNeeded = dosage.ifNeeded()
            if (ifNeeded != null && ifNeeded) {
                result = "if needed $result"
            }
            if (dosage.dosageUnit() == null) {
                result = "unknown prescription"
            } else if (dosage.dosageUnit()!!.matches(SPECIFIC_OR_UNKNOWN.toRegex())) {
                result = dosage.dosageUnit()!!
            } else if (dosage.dosageUnit() != null) {
                result += " " + dosage.dosageUnit()
            }
            if (("Cutaneous" == medication.administrationRoute() || "Intravenous" == medication.administrationRoute())
                && dosage.dosageMin() == 0.0
            ) {
                result = ""
            }
            return result
        }

        private fun frequency(dosage: Dosage): String {
            var result = if (dosage.frequency() != null) Formats.twoDigitNumber(dosage.frequency()!!) else "?"
            if (dosage.frequencyUnit() == null) {
                result = "unknown prescription"
            } else if (dosage.frequencyUnit()!!.matches((SPECIFIC_OR_UNKNOWN + "|once").toRegex())) {
                result = dosage.frequencyUnit()!!
            } else if (dosage.periodBetweenUnit() != null) {
                result += " / " + Formats.noDigitNumber(dosage.periodBetweenValue()!! + 1) + " " + dosage.periodBetweenUnit()
            } else if (dosage.frequencyUnit() != null) {
                result += " / " + dosage.frequencyUnit()
            }
            return result
        }
    }
}