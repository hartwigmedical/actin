package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHistoryOfSecondMalignancy: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.priorSecondPrimaries.isNotEmpty()) {
            EvaluationFactory.pass("Patient has second malignancy", "Presence of second malignancy")
        } else {
            EvaluationFactory.fail("Patient has no previous second malignancy", "No previous second malignancy")
        }
    }
}