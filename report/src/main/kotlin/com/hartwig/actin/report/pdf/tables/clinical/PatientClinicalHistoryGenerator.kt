package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.sort.PriorSecondPrimaryDiagnosedDateComparator
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells.create
import com.hartwig.actin.report.pdf.util.Cells.createKey
import com.hartwig.actin.report.pdf.util.Cells.createSpanningValue
import com.hartwig.actin.report.pdf.util.Cells.createValue
import com.hartwig.actin.report.pdf.util.Tables.createFixedWidthCols
import com.hartwig.actin.report.pdf.util.Tables.createSingleColWithWidth
import com.itextpdf.layout.element.BlockElement
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table

class PatientClinicalHistoryGenerator(private val record: ClinicalRecord, private val keyWidth: Float, private val valueWidth: Float) :
    TableGenerator {
    override fun title(): String {
        return "Clinical summary"
    }

    override fun contents(): Table {
        val table = createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(createKey("Relevant systemic treatment history"))
        table.addCell(create(tableOrNone(relevantSystemicPreTreatmentHistoryTable(record))))
        table.addCell(createKey("Relevant other oncological history"))
        table.addCell(create(tableOrNone(relevantNonSystemicPreTreatmentHistoryTable(record))))
        table.addCell(createKey("Previous primary tumor"))
        table.addCell(create(tableOrNone(secondPrimaryHistoryTable(record))))
        table.addCell(createKey("Relevant non-oncological history"))
        table.addCell(create(tableOrNone(relevantNonOncologicalHistoryTable(record))))
        return table
    }

    private fun tableOrNone(table: Table): Table {
        return if (table.numberOfRows > 0) table else createNoneTable()
    }

    private fun createNoneTable(): Table {
        val table: Table = createSingleColumnTable(
            valueWidth
        )
        table.addCell(createSpanningTableEntry("None", table))
        return table
    }

    private fun relevantSystemicPreTreatmentHistoryTable(record: ClinicalRecord): Table {
        return treatmentHistoryTable(record.treatmentHistory(), true)
    }

    private fun relevantNonSystemicPreTreatmentHistoryTable(record: ClinicalRecord): Table {
        return treatmentHistoryTable(record.treatmentHistory(), false)
    }

    private fun treatmentHistoryTable(treatmentHistory: List<TreatmentHistoryEntry>, requireSystemic: Boolean): Table {
        val dateWidth = valueWidth / 5
        val treatmentWidth = valueWidth - dateWidth
        val table: Table = createDoubleColumnTable(dateWidth, treatmentWidth)

        treatmentHistory.filter { treatmentHistoryEntryIsSystemic(it) == requireSystemic }
            .sortedWith(TreatmentHistoryAscendingDateComparator())
            .flatMap { listOf(extractDateRangeString(it), extractTreatmentString(it)) }
            .forEach { table.addCell(createSingleTableEntry(it)) }
        return table
    }

    private fun treatmentHistoryEntryIsSystemic(treatmentHistoryEntry: TreatmentHistoryEntry): Boolean {
        return treatmentHistoryEntry.treatments().any { it.isSystemic }
    }

    private fun secondPrimaryHistoryTable(record: ClinicalRecord): Table {
        val table: Table = createSingleColumnTable(valueWidth)

        record.priorSecondPrimaries().sortedWith(PriorSecondPrimaryDiagnosedDateComparator())
            .forEach { table.addCell(createSingleTableEntry(toSecondPrimaryString(it))) }

        return table
    }

    private fun relevantNonOncologicalHistoryTable(record: ClinicalRecord): Table {
        val dateWidth = valueWidth / 5
        val treatmentWidth = valueWidth - dateWidth
        val table: Table = createDoubleColumnTable(dateWidth, treatmentWidth)

        record.priorOtherConditions().forEach { priorOtherCondition: PriorOtherCondition ->
            val dateString = toDateString(priorOtherCondition.year(), priorOtherCondition.month())
            if (dateString != null) {
                table.addCell(createSingleTableEntry(dateString))
                table.addCell(createSingleTableEntry(toPriorOtherConditionString(priorOtherCondition)))
            } else {
                table.addCell(createSpanningTableEntry(toPriorOtherConditionString(priorOtherCondition), table))
            }
        }
        return table
    }

    companion object {
        private const val STOP_REASON_PROGRESSIVE_DISEASE = "PD"

        private fun extractDateRangeString(treatmentHistoryEntry: TreatmentHistoryEntry): String {
            val startString = toDateString(treatmentHistoryEntry.startYear(), treatmentHistoryEntry.startMonth()) ?: "?"
            return treatmentHistoryEntry.therapyHistoryDetails()?.let { toDateString(it.stopYear(), it.stopMonth()) }
                ?.let { stopString: String -> "$startString-$stopString" } ?: startString
        }

        private fun extractTreatmentString(treatmentHistoryEntry: TreatmentHistoryEntry): String {
            val intentString = treatmentHistoryEntry.intents()
                ?.filterNotNull()
                ?.filter{it != Intent.PALLIATIVE}
                ?.joinToString(" ") { it.name.lowercase() }

            val cyclesString = treatmentHistoryEntry.therapyHistoryDetails()?.cycles()?.let { "$it cycles" }

            val stopReasonString = treatmentHistoryEntry.therapyHistoryDetails()?.stopReasonDetail()
                ?.let { if (!it.equals(STOP_REASON_PROGRESSIVE_DISEASE, ignoreCase = true)) "stop reason: $it" else null }

            val combinedAnnotation = listOfNotNull(intentString, cyclesString, stopReasonString).joinToString(", ")

            val trialDisplay = if (treatmentHistoryEntry.isTrial) {
                "Clinical trial" + if (treatmentHistoryEntry.trialAcronym().isNullOrEmpty()) ": " else " (${treatmentHistoryEntry.trialAcronym()}): "
            } else {
                ""
            }

            return trialDisplay + treatmentHistoryEntry.treatmentDisplay() + if (combinedAnnotation.isEmpty()) "" else " ($combinedAnnotation)"
        }

        private fun toSecondPrimaryString(priorSecondPrimary: PriorSecondPrimary): String {
            val tumorLocation = priorSecondPrimary.tumorLocation()
            val tumorDetails = when {
                priorSecondPrimary.tumorSubType().isNotEmpty() -> {
                    tumorLocation + " " + priorSecondPrimary.tumorSubType().lowercase()
                }

                priorSecondPrimary.tumorType().isNotEmpty() -> {
                    tumorLocation + " " + priorSecondPrimary.tumorType().lowercase()
                }

                else -> tumorLocation
            }
            val dateAdditionDiagnosis: String = toDateString(priorSecondPrimary.diagnosedYear(), priorSecondPrimary.diagnosedMonth())
                ?.let { "diagnosed $it, " } ?: ""

            val dateAdditionLastTreatment = toDateString(priorSecondPrimary.lastTreatmentYear(), priorSecondPrimary.lastTreatmentMonth())
                ?.let { "last treatment $it, " } ?: ""

            val status = when (priorSecondPrimary.status()) {
                TumorStatus.ACTIVE -> "considered active"
                TumorStatus.INACTIVE -> "considered non-active"
                TumorStatus.EXPECTATIVE -> "considered expectative"
            }
            return "$tumorDetails ($dateAdditionDiagnosis$dateAdditionLastTreatment$status)"
        }

        private fun toPriorOtherConditionString(priorOtherCondition: PriorOtherCondition): String {
            val note = if (!priorOtherCondition.isContraindicationForTherapy) " (no contraindication for therapy)" else ""
            return priorOtherCondition.name() + note
        }

        private fun toDateString(maybeYear: Int?, maybeMonth: Int?): String? {
            return maybeYear?.let { year: Int ->
                maybeMonth?.let { month: Int -> "$month/$year" } ?: year.toString()
            }
        }

        private fun createDoubleColumnTable(column1Width: Float, column2Width: Float): Table {
            return removePadding<Table>(createFixedWidthCols(column1Width, column2Width))
        }

        private fun createSingleColumnTable(width: Float): Table {
            return removePadding<Table>(createSingleColWithWidth(width))
        }

        private fun createSingleTableEntry(value: String): Cell {
            return removePadding<Cell>(createValue(value))
        }

        private fun createSpanningTableEntry(value: String, table: Table): Cell {
            return removePadding<Cell>(createSpanningValue(value, table))
        }

        private fun <T : BlockElement<T>?> removePadding(table: T): T {
            table!!.setPadding(0f)
            return table
        }
    }
}