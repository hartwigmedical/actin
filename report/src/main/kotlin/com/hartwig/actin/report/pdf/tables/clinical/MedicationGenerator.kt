package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.medication.DosageFormatter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class MedicationGenerator(private val medications: List<Medication>, private val interpreter: MedicationStatusInterpreter) :
    TableGenerator {

    override fun title(): String = "Active medication details"

    override fun forceKeepTogether(): Boolean = false

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f, 1f, 1f, 1f)

        table.addHeaderCell(Cells.createHeader("Medication"))
        table.addHeaderCell(Cells.createHeader("Administration route"))
        table.addHeaderCell(Cells.createHeader("Start date"))
        table.addHeaderCell(Cells.createHeader("Stop date"))
        table.addHeaderCell(Cells.createHeader("Dosage"))
        table.addHeaderCell(Cells.createHeader("Frequency"))

        medications.distinct()
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .forEach { medication: Medication ->
                val hideDosage = medication.administrationRoute in setOf("Cutaneous", "Intravenous") && medication.dosage.dosageMin == 0.0
                table.addCell(Cells.createContent(medication.name))
                table.addCell(Cells.createContent(medication.administrationRoute ?: ""))
                table.addCell(Cells.createContent(Formats.date(medication.startDate, "")))
                table.addCell(Cells.createContent(Formats.date(medication.stopDate, "")))
                table.addCell(Cells.createContent(if (hideDosage) "" else DosageFormatter.formatDosage(medication.dosage)))
                table.addCell(Cells.createContent(DosageFormatter.formatFrequency(medication.dosage)))
            }

        return table
    }
}