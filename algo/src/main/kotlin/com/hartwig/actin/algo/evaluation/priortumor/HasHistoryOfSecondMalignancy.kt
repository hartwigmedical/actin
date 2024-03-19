package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHistoryOfSecondMalignancy internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.priorSecondPrimaries.isNotEmpty()) {
            EvaluationFactory.pass("Patient has second malignancy", "Presence of second malignancy")
        } else {
            EvaluationFactory.fail("Patient has no previous second malignancy", "No previous second malignancy")
        }
    }
}