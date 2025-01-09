package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasOralMedicationDifficulties: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (complication in record.complications ?: emptyList()) {
            if (stringCaseInsensitivelyMatchesQueryCollection(complication.name, COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES)) {
                return EvaluationFactory.pass("Potential oral medication difficulties: " + complication.name)
            }
        }
        return EvaluationFactory.fail("No potential oral medication difficulties")
    }

    companion object {
        val COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES = setOf("tube", "swallow")
    }
}