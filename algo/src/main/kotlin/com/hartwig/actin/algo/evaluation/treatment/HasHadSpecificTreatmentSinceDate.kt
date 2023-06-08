package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDate internal constructor(treatmentName: String, minDate: LocalDate) : EvaluationFunction {

    private val query: String
    private val minDate: LocalDate

    init {
        query = treatmentName.lowercase()
        this.minDate = minDate
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments: List<PriorTumorTreatment> = record.clinical().priorTumorTreatments()
            .filter { it.name().lowercase().contains(query) }

        return when {
            matchingTreatments.any { treatmentSinceMinDate(it, false) } ->
                EvaluationFactory.pass(
                    "Treatment matching '$query' administered since ${Format.date(minDate)}",
                    "Matching treatment since date"
                )

            matchingTreatments.any { treatmentSinceMinDate(it, true) } ->
                EvaluationFactory.undetermined(
                    "Treatment matching '$query' administered with unknown date",
                    "Matching treatment with unknown date"
                )

            matchingTreatments.isNotEmpty() ->
                EvaluationFactory.fail(
                    "All treatments matching '$query' administered before ${Format.date(minDate)}",
                    "Matching treatment with earlier date"
                )

            else ->
                EvaluationFactory.fail(
                    "No treatments matching '$query' in prior tumor history",
                    "No matching treatments found"
                )
        }
    }

    private fun treatmentSinceMinDate(treatment: PriorTumorTreatment, includeUnknown: Boolean): Boolean {
        return isAfterDate(minDate, treatment.stopYear(), treatment.stopMonth()) ?: isAfterDate(
            minDate, treatment.startYear(),
            treatment.startMonth()
        ) ?: includeUnknown
    }
}