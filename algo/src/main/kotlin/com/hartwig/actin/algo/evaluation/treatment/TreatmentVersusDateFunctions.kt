package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

object TreatmentVersusDateFunctions {

    fun evaluateTreatmentMatchingPredicateSinceDate(
        record: PatientRecord, minDate: LocalDate, predicateDescription: String, predicate: (Treatment) -> Boolean
    ): Evaluation {
        val matchingTreatments = record.oncologicalHistory
            .mapNotNull { entry -> TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate) }
        val formattedMinDate = Format.date(minDate)

        return when {
            matchingTreatments.any { certainTreatmentSinceMinDate(it, minDate) } -> {
                EvaluationFactory.pass("Treatment $predicateDescription administered since $formattedMinDate")
            }

            matchingTreatments.any { potentialTreatmentSinceMinDate(it, minDate) } -> {
                EvaluationFactory.undetermined(
                    "Undetermined if treatment $predicateDescription may have been administered since " +
                            "$formattedMinDate (missing stop date)"
                )
            }

            matchingTreatments.isNotEmpty() -> {
                EvaluationFactory.fail("All treatments $predicateDescription administered before $formattedMinDate")
            }

            else -> EvaluationFactory.fail("No treatments $predicateDescription in history")
        }
    }

    fun certainTreatmentSinceMinDate(treatment: TreatmentHistoryEntry, minDate: LocalDate): Boolean {
        return DateComparison.isAfterDate(
            minDate,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: DateComparison.isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: false
    }

    fun potentialTreatmentSinceMinDate(treatment: TreatmentHistoryEntry, minDate: LocalDate): Boolean {
        return DateComparison.isAfterDate(minDate, treatment.stopYear(), treatment.stopMonth()) != false
    }

    fun treatmentBeforeMaxDate(treatment: TreatmentHistoryEntry, maxDate: LocalDate, includeUnknown: Boolean): Boolean {
        // The maxStopDate might be more recent than the actual stopDate. Using stopYear() and stopMonth() here could result
        // in incorrect evaluation to 'false'
        return DateComparison.isBeforeDate(
            maxDate, treatment.treatmentHistoryDetails?.stopYear, treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: includeUnknown
    }
}