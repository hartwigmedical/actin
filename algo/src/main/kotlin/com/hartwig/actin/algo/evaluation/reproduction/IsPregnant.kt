package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.Gender

class IsPregnant internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.patient.gender == Gender.MALE) {
            EvaluationFactory.fail("Patient is male, hence won't be pregnant", "No pregnancy")
        } else {
            EvaluationFactory.notEvaluated("It is assumed that patient won't be pregnant", "Assumed not pregnant")
        }
    }
}