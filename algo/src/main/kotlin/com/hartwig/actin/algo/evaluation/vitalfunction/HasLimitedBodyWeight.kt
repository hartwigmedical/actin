package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasLimitedBodyWeight(val referenceWeight: Double): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BodyWeightFunctions.evaluatePatientBodyWeightAgainstMax(
            record, this.referenceWeight
        )
    }
}

