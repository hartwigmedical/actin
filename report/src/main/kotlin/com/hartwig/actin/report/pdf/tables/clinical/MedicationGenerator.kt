package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.medication.DosageFormatter
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class MedicationGenerator(
    private val medications: List<Medication>,
    private val interpreter: MedicationStatusInterpreter,
    private val labels: ReportLabels
) : TableGenerator {

    override fun title(): String = labels.clinicalDetails.medicationTitle()

    override fun forceKeepTogether(): Boolean = false

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f, 1f, 1f, 1f)

        table.addHeaderCell(Cells.createHeader(labels.clinicalDetails.colMedication()))
        table.addHeaderCell(Cells.createHeader(labels.clinicalDetails.colAdminRoute()))
        table.addHeaderCell(Cells.createHeader(labels.clinicalDetails.colStartDate()))
        table.addHeaderCell(Cells.createHeader(labels.clinicalDetails.colStopDate()))
        table.addHeaderCell(Cells.createHeader(labels.clinicalDetails.colDosage()))
        table.addHeaderCell(Cells.createHeader(labels.clinicalDetails.colFrequency()))

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