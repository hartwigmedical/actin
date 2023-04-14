package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

class HasHadSpecificTreatmentSinceDate internal constructor(treatmentName: String, minDate: LocalDate) : EvaluationFunction {
    private val query: String
    private val minDate: LocalDate

    init {
        query = treatmentName.lowercase(Locale.getDefault())
        this.minDate = minDate
    }

    fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments: Set<PriorTumorTreatment> = record.clinical()
                .priorTumorTreatments()
                .stream()
                .filter(Predicate { treatment: PriorTumorTreatment -> treatment.name().lowercase(Locale.getDefault()).contains(query) })
                .collect(Collectors.toSet<PriorTumorTreatment>())
        return if (matchingTreatments.stream().anyMatch(Predicate<PriorTumorTreatment> { treatment: PriorTumorTreatment -> treatmentSinceMinDate(treatment, false) })) {
            EvaluationFactory.pass(java.lang.String.format("Treatment matching '%s' administered since %s", query, Format.date(minDate)),
                    "Matching treatment since date")
        } else if (matchingTreatments.stream().anyMatch(Predicate<PriorTumorTreatment> { treatment: PriorTumorTreatment -> treatmentSinceMinDate(treatment, true) })) {
            EvaluationFactory.undetermined(String.format("Treatment matching '%s' administered with unknown date", query),
                    "Matching treatment with unknown date")
        } else if (!matchingTreatments.isEmpty()) {
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
            if (year > minDate.getYear()) {
                return@flatMap Optional.of(true)
            } else if (year == minDate.getYear()) {
                return@flatMap Optional.ofNullable(nullableMonth).map(Function<Int, Boolean> { month: Int -> month >= minDate.getMonthValue() })
            } else {
                return@flatMap Optional.of(false)
            }
        }
    }
}