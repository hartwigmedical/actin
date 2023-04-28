package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection

class HasOralMedicationDifficulties internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        for (complication in record.clinical().complications() ?: emptyList()) {
            if (stringCaseInsensitivelyMatchesQueryCollection(complication.name(), COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES)) {
                return EvaluationFactory.pass(
                    "Patient has potential oral medication difficulties due to " + complication.name(),
                    "Potential oral medication difficulties: " + complication.name()
                )
            }
        }
        return EvaluationFactory.fail(
            "No potential reasons for difficulty with oral medication identified",
            "No potential oral medication difficulties identified"
        )
    }

    companion object {
        val COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES = setOf("tube", "swallow")
    }
}