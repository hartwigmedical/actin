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

        return when {
            matchingTreatments.any { treatmentSinceMinDate(it, minDate, false) } ->
                EvaluationFactory.pass("Treatment $predicateDescription administered since ${Format.date(minDate)}")

            matchingTreatments.any { treatmentSinceMinDate(it, minDate, true) } ->
                EvaluationFactory.undetermined("Treatment $predicateDescription administered with unknown date")

            matchingTreatments.isNotEmpty() ->
                EvaluationFactory.fail("All treatments $predicateDescription administered before ${Format.date(minDate)}")

            else ->
                EvaluationFactory.fail("No treatments $predicateDescription in prior history")
        }
    }

    fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, minDate: LocalDate, includeUnknown: Boolean): Boolean {
        return DateComparison.isAfterDate(minDate, treatment.stopYear(), treatment.stopMonth())
            ?: DateComparison.isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: includeUnknown
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