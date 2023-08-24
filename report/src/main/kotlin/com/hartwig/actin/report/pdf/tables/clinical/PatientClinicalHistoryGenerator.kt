package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparatorFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class PatientClinicalHistoryGenerator(private val record: ClinicalRecord, private val keyWidth: Float, private val valueWidth: Float) :
    TableGenerator {
    override fun title(): String {
        return "Clinical summary"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("Relevant systemic treatment history"))
        table.addCell(Cells.createValue(relevantSystemicPreTreatmentHistory(record)))
        table.addCell(Cells.createKey("Relevant other oncological history"))
        table.addCell(Cells.createValue(relevantNonSystemicPreTreatmentHistory(record).ifEmpty { Formats.VALUE_NONE }))
        table.addCell(Cells.createKey("Previous primary tumor"))
        table.addCell(Cells.createValue(secondPrimaryHistory(record).ifEmpty { Formats.VALUE_NONE }))
        table.addCell(Cells.createKey("Relevant non-oncological history"))
        table.addCell(Cells.createValue(relevantNonOncologicalHistory(record)))
        return table
    }

    companion object {
        private fun relevantSystemicPreTreatmentHistory(record: ClinicalRecord): String {
            return treatmentHistoryString(record.treatmentHistory(), true)
        }

        private fun relevantNonSystemicPreTreatmentHistory(record: ClinicalRecord): String {
            return treatmentHistoryString(record.treatmentHistory(), false)
        }

        private fun treatmentHistoryString(treatmentHistory: List<TreatmentHistoryEntry>, isSystemic: Boolean): String {
            val sortedFilteredTreatments = treatmentHistory.filter { treatmentHistoryEntryIsSystemic(it) == isSystemic }
                .sortedWith(TreatmentHistoryAscendingDateComparatorFactory.treatmentHistoryEntryComparator())

            val treatmentsByName = sortedFilteredTreatments.groupBy(TreatmentHistoryEntry::treatmentName)

            val evaluatedNames: MutableSet<String> = mutableSetOf()
            val annotationString = sortedFilteredTreatments.mapNotNull { treatment: TreatmentHistoryEntry ->
                val treatmentName = treatment.treatmentName()
                if (!evaluatedNames.contains(treatmentName)) {
                    evaluatedNames.add(treatmentName)
                    val annotationOption = treatmentsByName[treatmentName]!!
                        .mapNotNull(::extractAnnotationForTreatment)
                        .joinToString("; ").ifEmpty { null }
                    // TODO: use treatmentHistoryEntry.treatmentDisplay() here
                    treatmentName + annotationOption?.let { " ($it)" }
                } else {
                    null
                }
            }.joinToString(Formats.COMMA_SEPARATOR)
            return Formats.valueOrDefault(annotationString, Formats.VALUE_NONE)
        }

        private fun treatmentHistoryEntryIsSystemic(treatmentHistoryEntry: TreatmentHistoryEntry): Boolean {
            return treatmentHistoryEntry.treatments().any { it.isSystemic }
        }

        private fun extractAnnotationForTreatment(treatmentHistoryEntry: TreatmentHistoryEntry): String? {
            return listOfNotNull(
                toDateRangeString(treatmentHistoryEntry),
                toNumberOfCyclesString(treatmentHistoryEntry),
                toStopReasonString(treatmentHistoryEntry)
            ).joinToString(Formats.COMMA_SEPARATOR).ifEmpty { null }
        }

        private fun secondPrimaryHistory(record: ClinicalRecord): String {
            return record.priorSecondPrimaries().joinToString(Formats.COMMA_SEPARATOR) { priorSecondPrimary ->
                val tumorLocation = priorSecondPrimary.tumorLocation()
                val tumorDetails = when {
                    priorSecondPrimary.tumorSubType().isNotEmpty() -> {
                        tumorLocation + " " + priorSecondPrimary.tumorSubType()
                    }

                    priorSecondPrimary.tumorType().isNotEmpty() -> {
                        tumorLocation + " " + priorSecondPrimary.tumorType()
                    }

                    else -> tumorLocation
                }

                val dateAdditionDiagnosis = toDateString(priorSecondPrimary.diagnosedYear(), priorSecondPrimary.diagnosedMonth())
                    ?.let { ("diagnosed $it, ") } ?: ""

                val dateAdditionLastTreatment = toDateString(
                    priorSecondPrimary.lastTreatmentYear(),
                    priorSecondPrimary.lastTreatmentMonth()
                )?.let { "last treatment $it, " } ?: ""

                val active = if (priorSecondPrimary.isActive) "considered active" else "considered non-active"
                "$tumorDetails ($dateAdditionDiagnosis$dateAdditionLastTreatment$active)"
            }
        }

        private fun toDateRangeString(treatmentHistoryEntry: TreatmentHistoryEntry): String? {
            val startOption = toDateString(treatmentHistoryEntry.startYear(), treatmentHistoryEntry.startMonth())
            val stopOption = treatmentHistoryEntry.therapyHistoryDetails()?.let { toDateString(it.stopYear(), it.stopMonth()) }
            return startOption?.let { startString ->
                startString + stopOption?.let { stopString -> "-$stopString" }
            } ?: stopOption?.let { "end: $it" }
        }

        private fun toDateString(maybeYear: Int?, maybeMonth: Int?): String? {
            return maybeYear?.let { year: Int ->
                maybeMonth?.let { month: Int -> "$month/$year" } ?: year.toString()
            }
        }

        private fun toNumberOfCyclesString(treatmentHistoryEntry: TreatmentHistoryEntry): String? {
            return treatmentHistoryEntry.therapyHistoryDetails()?.let { "${it.cycles()} cycles" }
        }

        private fun toStopReasonString(treatmentHistoryEntry: TreatmentHistoryEntry): String? {
            return treatmentHistoryEntry.therapyHistoryDetails()?.let { "stop reason: ${it.stopReasonDetail()}" }
        }

        private fun relevantNonOncologicalHistory(record: ClinicalRecord): String {
            val relevantHistory = record.priorOtherConditions().joinToString(", ") { priorOtherCondition ->
                val addon = if (!priorOtherCondition.isContraindicationForTherapy) " (no contraindication for therapy)" else ""
                val dateAddition = toDateString(priorOtherCondition.year(), priorOtherCondition.month())?.let { " ($it)" } ?: ""
                priorOtherCondition.name() + dateAddition + addon
            }
            return Formats.valueOrDefault(relevantHistory, Formats.VALUE_NONE)
        }
    }
}