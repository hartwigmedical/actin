package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

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
        medications.distinct()
            .filter { it.status()?.display() == "Active" || it.status()?.display() == "Planned" }
            .forEach { medication: Medication ->
                table.addCell(Cells.createContent(medication.name()))
                table.addCell(Cells.createContent(administrationRoute(medication)))
                table.addCell(Cells.createContent(Formats.date(medication.startDate(), "")))
                table.addCell(Cells.createContent(Formats.date(medication.stopDate(), "")))
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
            if (medication.administrationRoute() in setOf("Cutaneous", "Intravenous") && dosage.dosageMin() == 0.0) {
                return ""
            }
            val dosageMin = formatDosageLimit(dosage.dosageMin())
            val dosageMax = formatDosageLimit(dosage.dosageMax())
            val dosageString = if (dosageMin == dosageMax) dosageMin else "$dosageMin - $dosageMax"
            val result = if (dosage.ifNeeded() == true) "if needed $dosageString" else dosageString

            val dosageUnit = dosage.dosageUnit()
            return when {
                dosageUnit == null -> {
                    "unknown prescription"
                }

                dosageUnit.matches(SPECIFIC_OR_UNKNOWN.toRegex()) -> {
                    dosageUnit
                }

                else -> {
                    "$result $dosageUnit"
                }
            }
        }

        private fun formatDosageLimit(dosageLimit: Double?) =
            if (dosageLimit != null && dosageLimit != 0.0) Formats.twoDigitNumber(dosageLimit) else "?"

        private fun frequency(dosage: Dosage): String {
            val frequency = if (dosage.frequency() != null) Formats.twoDigitNumber(dosage.frequency()!!) else "?"
            val frequencyUnit = dosage.frequencyUnit()
            return when {
                frequencyUnit == null -> {
                    "unknown prescription"
                }

                frequencyUnit.matches(("$SPECIFIC_OR_UNKNOWN|once").toRegex()) -> {
                    frequencyUnit
                }

                dosage.periodBetweenUnit() != null -> {
                    "$frequency / ${Formats.noDigitNumber(dosage.periodBetweenValue()!! + 1)} ${dosage.periodBetweenUnit()}"
                }

                else -> {
                    "$frequency / $frequencyUnit"
                }
            }
        }
    }
}