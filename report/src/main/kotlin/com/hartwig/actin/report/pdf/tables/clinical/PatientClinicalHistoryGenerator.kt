package com.hartwig.actin.report.pdf.tables.clinical

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparatorFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.util.Strings
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

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
        val nonSystemicHistory = relevantNonSystemicPreTreatmentHistory(record)
        if (!nonSystemicHistory.isEmpty()) {
            table.addCell(Cells.createValue(nonSystemicHistory))
        } else {
            table.addCell(Cells.createValue("None"))
        }
        table.addCell(Cells.createKey("Previous primary tumor"))
        val secondPrimaryHistory = secondPrimaryHistory(record)
        if (!secondPrimaryHistory.isEmpty()) {
            table.addCell(Cells.createValue(secondPrimaryHistory))
        } else {
            table.addCell(Cells.createValue("None"))
        }
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
            val sortedFilteredTreatments = treatmentHistory.stream()
                .filter { entry: TreatmentHistoryEntry ->
                    entry.treatments().stream().anyMatch { treatment: Treatment -> treatment.isSystemic == isSystemic }
                }
                .sorted(TreatmentHistoryAscendingDateComparatorFactory.treatmentHistoryEntryComparator())
                .collect(Collectors.toList())
            val treatmentsByName = sortedFilteredTreatments.stream().collect(
                Collectors.groupingBy(
                    Function { obj: TreatmentHistoryEntry -> obj.treatmentName() })
            )
            val evaluatedNames: MutableSet<String> = Sets.newHashSet()
            val annotationStream = sortedFilteredTreatments.stream().map { treatment: TreatmentHistoryEntry ->
                val treatmentName = treatment.treatmentName()
                if (!evaluatedNames.contains(treatmentName)) {
                    evaluatedNames.add(treatmentName)
                    val annotationOption = treatmentsByName[treatmentName]
                        .stream()
                        .map { treatmentHistoryEntry: TreatmentHistoryEntry -> extractAnnotationForTreatment(treatmentHistoryEntry) }
                        .flatMap { obj: Optional<String> -> obj.stream() }
                        .reduce { x: String, y: String -> "$x; $y" }
                    return@map Optional.of(treatmentName + annotationOption.map { annotation: String -> " ($annotation)" }
                        .orElse(""))
                } else {
                    return@map Optional.empty<String>()
                }
            }
            val annotationString = annotationStream.flatMap { obj: Optional<String> -> obj.stream() }.collect(Collectors.joining(", "))
            return Formats.valueOrDefault(annotationString, "None")
        }

        private fun extractAnnotationForTreatment(treatmentHistoryEntry: TreatmentHistoryEntry): Optional<String> {
            return Stream.of(
                toDateRangeString(treatmentHistoryEntry),
                toNumberOfCyclesString(treatmentHistoryEntry),
                toStopReasonString(treatmentHistoryEntry)
            ).flatMap { obj: Optional<String> -> obj.stream() }
                .reduce { x: String, y: String -> "$x, $y" }
        }

        private fun secondPrimaryHistory(record: ClinicalRecord): String {
            val joiner = Formats.commaJoiner()
            for (priorSecondPrimary in record.priorSecondPrimaries()) {
                var tumorDetails = priorSecondPrimary.tumorLocation()
                if (!priorSecondPrimary.tumorSubType().isEmpty()) {
                    tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorSubType()
                } else if (priorSecondPrimary.tumorSubType().isEmpty() && !priorSecondPrimary.tumorType().isEmpty()) {
                    tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorType()
                }
                val dateAdditionDiagnosis =
                    toDateString(priorSecondPrimary.diagnosedYear(), priorSecondPrimary.diagnosedMonth()).map { dateDiagnosis: String ->
                        ("diagnosed "
                                + dateDiagnosis + ", ")
                    }.orElse(Strings.EMPTY)
                val dateAdditionLastTreatment = toDateString(
                    priorSecondPrimary.lastTreatmentYear(),
                    priorSecondPrimary.lastTreatmentMonth()
                ).map { dateLastTreatment: String -> "last treatment $dateLastTreatment, " }
                    .orElse(Strings.EMPTY)
                val active = if (priorSecondPrimary.isActive) "considered active" else "considered non-active"
                joiner.add("$tumorDetails ($dateAdditionDiagnosis$dateAdditionLastTreatment$active)")
            }
            return joiner.toString()
        }

        private fun toDateRangeString(treatmentHistoryEntry: TreatmentHistoryEntry): Optional<String> {
            val startOption = toDateString(treatmentHistoryEntry.startYear(), treatmentHistoryEntry.startMonth())
            val stopOption = Optional.ofNullable(treatmentHistoryEntry.therapyHistoryDetails())
                .flatMap { details: TherapyHistoryDetails -> toDateString(details.stopYear(), details.stopMonth()) }
            return startOption.map { startString: String ->
                startString + stopOption.map { stopString: String -> "-$stopString" }
                    .orElse("")
            }
                .or { stopOption.map { stopString: String -> "end: $stopString" } }
        }

        private fun toDateString(maybeYear: Int?, maybeMonth: Int?): Optional<String> {
            return Optional.ofNullable(maybeYear)
                .map { year: Int ->
                    Optional.ofNullable(maybeMonth).map { month: Int -> "$month/$year" }
                        .orElse(year.toString())
                }
        }

        private fun toNumberOfCyclesString(treatmentHistoryEntry: TreatmentHistoryEntry): Optional<String> {
            return Optional.ofNullable(treatmentHistoryEntry.therapyHistoryDetails())
                .flatMap { details: TherapyHistoryDetails -> Optional.ofNullable(details.cycles()) }
                .map { num: Int -> "$num cycles" }
        }

        private fun toStopReasonString(treatmentHistoryEntry: TreatmentHistoryEntry): Optional<String> {
            return Optional.ofNullable(treatmentHistoryEntry.therapyHistoryDetails())
                .flatMap { details: TherapyHistoryDetails -> Optional.ofNullable(details.stopReasonDetail()) }
                .map { reason: String -> "stop reason: $reason" }
        }

        private fun relevantNonOncologicalHistory(record: ClinicalRecord): String {
            val joiner = Formats.commaJoiner()
            for (priorOtherCondition in record.priorOtherConditions()) {
                var addon = Strings.EMPTY
                if (!priorOtherCondition.isContraindicationForTherapy) {
                    addon = " (no contraindication for therapy)"
                }
                val dateOption = toDateString(priorOtherCondition.year(), priorOtherCondition.month())
                val dateAddition = dateOption.map { date: String -> " ($date)" }.orElse(Strings.EMPTY)
                joiner.add(priorOtherCondition.name() + dateAddition + addon)
            }
            return Formats.valueOrDefault(joiner.toString(), "None")
        }
    }
}