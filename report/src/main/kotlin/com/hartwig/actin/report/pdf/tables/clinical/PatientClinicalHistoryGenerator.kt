package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.sort.OtherConditionDescendingDateComparator
import com.hartwig.actin.clinical.sort.PriorPrimaryDiagnosedDateComparator
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells.create
import com.hartwig.actin.report.pdf.util.Cells.createKey
import com.hartwig.actin.report.pdf.util.Cells.createSpanningValue
import com.hartwig.actin.report.pdf.util.Cells.createValue
import com.hartwig.actin.report.pdf.util.Formats.DATE_UNKNOWN
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.BlockElement
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table

private const val STOP_REASON_PROGRESSIVE_DISEASE = "PD"

class PatientClinicalHistoryGenerator(
    private val report: Report,
    private val showDetails: Boolean,
    private val keyWidth: Float,
    private val valueWidth: Float
) : TableGenerator {

    override fun title(): String {
        return "Clinical summary"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        contentsAsList().forEach(table::addCell)
        return table
    }

    fun contentsAsList(): List<Cell> {
        val record = report.patientRecord
        return listOfNotNull(
            "Relevant systemic treatment history" to relevantSystemicPreTreatmentHistoryTable(record),
            if (report.config.includeOtherOncologicalHistoryInSummary || showDetails) {
                "Relevant other oncological history" to relevantNonSystemicPreTreatmentHistoryTable(record)
            } else null,
            if (report.config.includePreviousPrimaryInClinicalSummary || showDetails) {
                "Previous primary tumor" to priorPrimaryHistoryTable(record)
            } else null,
            if (report.config.includeRelevantNonOncologicalHistoryInSummary || showDetails) {
                "Relevant non-oncological history" to relevantNonOncologicalHistoryTable(record)
            } else null
        ).flatMap { (key, table) -> sequenceOf(createKey(key), create(tableOrNone(table))) }
    }

    private fun tableOrNone(table: Table): Table {
        return if (table.numberOfRows > 0) table else createNoneTable()
    }

    private fun createNoneTable(): Table {
        val table: Table = createSingleColumnTable(valueWidth)
        table.addCell(createSpanningTableEntry("None", table))
        return table
    }

    private fun relevantSystemicPreTreatmentHistoryTable(record: PatientRecord): Table {
        return treatmentHistoryTable(record.oncologicalHistory, record.medications ?: emptyList(), true)
    }

    private fun relevantNonSystemicPreTreatmentHistoryTable(record: PatientRecord): Table {
        return treatmentHistoryTable(record.oncologicalHistory, emptyList(), false)
    }

    private fun treatmentHistoryTable(
        treatmentHistory: List<TreatmentHistoryEntry>,
        medications: List<Medication>,
        requireSystemic: Boolean
    ): Table {
        val dateWidth = valueWidth / 5
        val treatmentWidth = valueWidth - dateWidth
        val table: Table = createDoubleColumnTable(dateWidth, treatmentWidth)

        val medicationsToAdd = MedicationToTreatmentConverter.convert(medications, treatmentHistory)
        val systemicTreatmentHistory = treatmentHistory.filter { treatmentHistoryEntryIsSystemic(it) == requireSystemic }

        (systemicTreatmentHistory + medicationsToAdd).sortedWith(TreatmentHistoryAscendingDateComparator())
            .groupBy { Triple(extractTreatmentString(it), it.startMonth, it.startYear) }
            .forEach { (key, historyEntries) ->
                val details =
                    historyEntries.flatMap {
                        it.treatmentHistoryDetails?.bodyLocations ?: emptySet()
                    }.joinToString(", ")
                listOf(extractDateRangeString(historyEntries.first()), key.first + if (details.isNotEmpty()) " ($details)" else "")
                    .forEach { table.addCell(createSingleTableEntry(it)) }
            }

        return table
    }

    private fun treatmentHistoryEntryIsSystemic(treatmentHistoryEntry: TreatmentHistoryEntry): Boolean {
        return treatmentHistoryEntry.allTreatments().any { it.isSystemic }
    }

    private fun priorPrimaryHistoryTable(record: PatientRecord): Table {
        val table: Table = createSingleColumnTable(valueWidth)

        record.priorPrimaries.distinct().sortedWith(PriorPrimaryDiagnosedDateComparator())
            .forEach { table.addCell(createSingleTableEntry(toPriorPrimaryString(it))) }

        return table
    }

    private fun relevantNonOncologicalHistoryTable(record: PatientRecord): Table {
        val dateWidth = valueWidth / 5
        val treatmentWidth = valueWidth - dateWidth
        val table: Table = createDoubleColumnTable(dateWidth, treatmentWidth)

        val anyDateIsKnown = record.otherConditions.any { toDateString(it.year, it.month) != null }

        record.otherConditions.sortedWith(OtherConditionDescendingDateComparator())
            .forEach { otherCondition: OtherCondition ->
                val dateString = toDateString(otherCondition.year, otherCondition.month)
                if (anyDateIsKnown) {
                    table.addCell(createSingleTableEntry(dateString ?: DATE_UNKNOWN))
                    table.addCell(createSingleTableEntry(toOtherConditionString(otherCondition)))
                } else {
                    table.addCell(createSpanningTableEntry(toOtherConditionString(otherCondition), table))
                }
            }
        return table
    }

    private fun extractDateRangeString(treatmentHistoryEntry: TreatmentHistoryEntry): String {
        val startString = toDateString(treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
        val stopString = treatmentHistoryEntry.treatmentHistoryDetails?.let { toDateString(it.stopYear, it.stopMonth) }

        return when {
            startString != null && stopString != null -> if (startString != stopString) "$startString-$stopString" else startString
            startString != null -> startString
            stopString != null -> "?-$stopString"
            else -> DATE_UNKNOWN
        }
    }

    private fun extractTreatmentString(treatmentHistoryEntry: TreatmentHistoryEntry): String {
        val intentNames = treatmentHistoryEntry.intents
            ?.filter { it != Intent.PALLIATIVE }
            ?.map { it.name.lowercase() }

        val intentString = when {
            intentNames.isNullOrEmpty() -> null
            intentNames.size == 1 -> intentNames[0]
            intentNames.size >= 2 -> {
                intentNames.dropLast(1).joinToString(", ") + " and " + intentNames.last()
            }

            else -> null
        }

        val cyclesString = treatmentHistoryEntry.treatmentHistoryDetails?.cycles?.let { if (it == 1) "$it cycle" else "$it cycles" }

        val stopReasonString = treatmentHistoryEntry.treatmentHistoryDetails?.stopReasonDetail
            ?.let { if (!it.equals(STOP_REASON_PROGRESSIVE_DISEASE, ignoreCase = true)) "stop reason: $it" else null }

        val annotation = listOfNotNull(intentString, cyclesString, stopReasonString).joinToString(", ")

        val treatmentWithAnnotation = listOfNotNull(
            treatmentHistoryEntry.treatmentDisplay() + if (annotation.isEmpty()) "" else " ($annotation)",
            treatmentHistoryEntry.treatmentHistoryDetails?.switchToTreatments?.ifEmpty { null }?.let { switchToTreatments ->
                switchToTreatments.joinToString(prefix = "with switch to ", separator = " then ") {
                    it.treatment.display() + (it.cycles?.let { cycles -> " (${cycles} cycles)" } ?: "")
                }
            },
            treatmentHistoryEntry.treatmentHistoryDetails?.maintenanceTreatment?.let { maintenanceTreatment ->
                "continued with ${maintenanceTreatment.treatment.display()} maintenance"
            }
        ).joinToString(" ")

        return if (treatmentHistoryEntry.isTrial) {
            val acronym = if (treatmentHistoryEntry.trialAcronym.isNullOrEmpty()) "" else "(${treatmentHistoryEntry.trialAcronym})"
            val trial = "Clinical trial"
            when {
                acronym.isEmpty() && treatmentWithAnnotation.isEmpty() -> "$trial (details unknown)"
                acronym.isNotEmpty() && treatmentWithAnnotation.isEmpty() -> "$trial $acronym"
                acronym.isEmpty() && treatmentWithAnnotation.isNotEmpty() -> "$trial: $treatmentWithAnnotation"
                else -> "$trial $acronym: $treatmentWithAnnotation"
            }
        } else {
            treatmentWithAnnotation
        }
    }

    private fun toPriorPrimaryString(priorPrimary: PriorPrimary): String {
        val dateAdditionDiagnosis: String = toDateString(priorPrimary.diagnosedYear, priorPrimary.diagnosedMonth)
            ?.let { "diagnosed $it, " } ?: ""

        val dateAdditionLastTreatment = toDateString(priorPrimary.lastTreatmentYear, priorPrimary.lastTreatmentMonth)
            ?.let { "last treatment $it, " } ?: ""

        val status = when (priorPrimary.status) {
            TumorStatus.ACTIVE -> "considered active"
            TumorStatus.INACTIVE -> "considered non-active"
            TumorStatus.EXPECTATIVE -> "considered expectative"
            TumorStatus.UNKNOWN -> "unknown if active"
        }
        return "${priorPrimary.name} ($dateAdditionDiagnosis$dateAdditionLastTreatment$status)"
    }

    private fun toOtherConditionString(otherCondition: OtherCondition): String {
        return otherCondition.display().replaceFirstChar(Char::uppercase)
    }

    private fun toDateString(maybeYear: Int?, maybeMonth: Int?): String? {
        return maybeYear?.let { year: Int ->
            maybeMonth?.let { month: Int -> "$month/$year" } ?: year.toString()
        }
    }

    private fun createDoubleColumnTable(column1Width: Float, column2Width: Float): Table {
        return removePadding<Table>(Tables.createFixedWidthCols(column1Width, column2Width))
    }

    private fun createSingleColumnTable(width: Float): Table {
        return removePadding<Table>(Tables.createSingleColWithWidth(width))
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