package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions.treatmentSinceMinDate
import java.time.LocalDate

class HasHadAnyCancerTreatmentSinceDate(
    private val minDate: LocalDate,
    private val monthsAgo: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorCancerTreatment = record.oncologicalHistory
        val concatenatedTreatmentDisplay = priorCancerTreatment.filter { treatmentSinceMinDate(it, minDate, true) }.toSet()
            .joinToString { it.treatmentDisplay() }

        return when {
            priorCancerTreatment.any { treatmentSinceMinDate(it, minDate, false) } -> {
                EvaluationFactory.pass(
                    "Patient has had anti-cancer therapy ($concatenatedTreatmentDisplay) within the last $monthsAgo months",
                    "Received anti-cancer therapy ($concatenatedTreatmentDisplay) within the last $monthsAgo months"
                )
            }

            priorCancerTreatment.any { treatmentSinceMinDate(it, minDate, true)
            } -> {
                EvaluationFactory.undetermined(
                    "Patient has had anti-cancer therapy ($concatenatedTreatmentDisplay) but " +
                            "undetermined if in the last $monthsAgo months (date unknown)",
                    "Received anti-cancer therapy ($concatenatedTreatmentDisplay) but " +
                            "undetermined if in the last $monthsAgo months (date unknown)"
                )
            }

            priorCancerTreatment.isEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has not received anti-cancer therapy within $monthsAgo months",
                    "Has not received anti-cancer therapy within $monthsAgo months"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not had any prior cancer treatments",
                    "Has not had any cancer treatment"
                )
            }
        }
    }
}
