package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator

class HasSufficientBodyWeight internal constructor(private val minBodyWeight: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecent = record.clinical().bodyWeights().sortedWith(BodyWeightDescendingDateComparator()).firstOrNull()
            ?: return EvaluationFactory.undetermined("No body weights found", "No body weights found")

        if (!mostRecent.unit().equals(EXPECTED_UNIT, ignoreCase = true)) {
            return EvaluationFactory.undetermined(
                "Most recent body weight not measured in $EXPECTED_UNIT",
                "Invalid body weight unit"
            )
        }
        return if (mostRecent.value().compareTo(minBodyWeight) >= 0) {
            EvaluationFactory.pass("Patient has body weight above $minBodyWeight", "Body weight above $minBodyWeight")
        } else {
            EvaluationFactory.fail("Patient has body weight below $minBodyWeight", "Body weight below $minBodyWeight")
        }
    }

    companion object {
        const val EXPECTED_UNIT: String = "kilogram"
    }
}