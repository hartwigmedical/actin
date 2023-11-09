package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator

class HasLimitedBodyWeight internal constructor(private val maxBodyWeight: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecent = record.clinical().bodyWeights().sortedWith(BodyWeightDescendingDateComparator()).firstOrNull()
            ?: return EvaluationFactory.undetermined("No body weights found", "No body weights found")

        if (!mostRecent.unit().equals(EXPECTED_UNIT, ignoreCase = true)) {
            return EvaluationFactory.undetermined(
                "Most recent body weight not measured in ${EXPECTED_UNIT}",
                "Invalid body weight unit"
            )
        }
        return if (mostRecent.value().compareTo(maxBodyWeight) <= 0) {
            EvaluationFactory.pass("Patient has body weight below $maxBodyWeight", "Body weight below $maxBodyWeight")
        } else {
            EvaluationFactory.fail("Patient has body weight above $maxBodyWeight", "Body weight above $maxBodyWeight")
        }
    }

    companion object {
        const val EXPECTED_UNIT: String = "kilogram"
    }
}

