package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasActiveSecondMalignancy internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.clinical().priorSecondPrimaries().any { it.isActive }) {
            EvaluationFactory.pass("Patient has second malignancy considered active", "Presence of second malignancy considered active")
        } else {
            EvaluationFactory.fail("Patient has no active second malignancy", "No active second malignancy")
        }
    }
}