package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction
import com.hartwig.actin.soc.evaluation.util.Format
import java.time.LocalDate
import java.util.*
import java.util.function.Predicate

class HasHadSpecificTreatmentSinceDate internal constructor(treatmentName: String, minDate: LocalDate) : EvaluationFunction {
    private val query: String
    private val minDate: LocalDate

    init {
        query = treatmentName.lowercase(Locale.getDefault())
        this.minDate = minDate
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments: Set<PriorTumorTreatment> = record.clinical()
                .priorTumorTreatments()
                .filter { treatment: PriorTumorTreatment -> treatment.name().lowercase(Locale.getDefault()).contains(query) }
                .toSet()
        return if (matchingTreatments.any { treatment: PriorTumorTreatment -> treatmentSinceMinDate(treatment, false) }) {
            EvaluationFactory.pass(java.lang.String.format("Treatment matching '%s' administered since %s", query, Format.date(minDate)),
                    "Matching treatment since date")
        } else if (matchingTreatments.stream().anyMatch(Predicate<PriorTumorTreatment> { treatment: PriorTumorTreatment -> treatmentSinceMinDate(treatment, true) })) {
            EvaluationFactory.undetermined(String.format("Treatment matching '%s' administered with unknown date", query),
                    "Matching treatment with unknown date")
        } else if (matchingTreatments.isNotEmpty()) {
            EvaluationFactory.fail(java.lang.String.format("All treatments matching '%s' administered before %s", query, Format.date(minDate)),
                    "Matching treatment with earlier date")
        } else {
            EvaluationFactory.fail(String.format("No treatments matching '%s' in prior tumor history", query),
                    "No matching treatments found")
        }
    }

    private fun treatmentSinceMinDate(treatment: PriorTumorTreatment, includeUnknown: Boolean): Boolean {
        return yearAndMonthSinceMinDate(treatment.stopYear(), treatment.stopMonth()).orElse(yearAndMonthSinceMinDate(treatment.startYear(),
                treatment.startMonth()).orElse(includeUnknown))
    }

    private fun yearAndMonthSinceMinDate(nullableYear: Int?, nullableMonth: Int?): Optional<Boolean> {
        return Optional.ofNullable(nullableYear).flatMap { year: Int ->
            if (year > minDate.year) {
                return@flatMap Optional.of(true)
            } else if (year == minDate.year) {
                return@flatMap Optional.ofNullable(nullableMonth).map { month: Int -> month >= minDate.monthValue }
            } else {
                return@flatMap Optional.of(false)
            }
        }
    }
}