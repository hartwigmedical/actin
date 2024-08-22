package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.Gender

class IsFemale: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return when (record.patient.gender) {
            Gender.FEMALE -> EvaluationFactory.pass("Patient is female", "Adequate gender")
            else -> EvaluationFactory.fail("Patient is not female", "Inadequate gender")
        }
    }
}