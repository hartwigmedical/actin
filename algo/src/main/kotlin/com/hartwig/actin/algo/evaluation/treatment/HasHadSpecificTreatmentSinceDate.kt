package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDate(private val treatment: Treatment, private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments: List<TreatmentHistoryEntry> = record.clinical.oncologicalHistory
            .mapNotNull { entry ->
                TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry) { it.name == treatment.name }
            }

        return when {
            matchingTreatments.any { treatmentSinceMinDate(it, false) } ->
                EvaluationFactory.pass(
                    "Treatment matching '${treatment.display()}' administered since ${Format.date(minDate)}",
                    "Matching treatment since date"
                )

            matchingTreatments.any { treatmentSinceMinDate(it, true) } ->
                EvaluationFactory.undetermined(
                    "Treatment matching '${treatment.display()}' administered with unknown date",
                    "Matching treatment with unknown date"
                )

            matchingTreatments.isNotEmpty() ->
                EvaluationFactory.fail(
                    "All treatments matching '${treatment.display()}' administered before ${Format.date(minDate)}",
                    "Matching treatment with earlier date"
                )

            else ->
                EvaluationFactory.fail(
                    "No treatments matching '${treatment.display()}' in prior tumor history",
                    "No matching treatments found"
                )
        }
    }

    private fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, includeUnknown: Boolean): Boolean {
        return isAfterDate(minDate, treatment.treatmentHistoryDetails?.stopYear, treatment.treatmentHistoryDetails?.stopMonth)
            ?: isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: includeUnknown
    }
}