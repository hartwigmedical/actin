package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Gender

class IsFemale(private val labels: EvaluationLabels.General) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (record.patient.gender) {
            Gender.FEMALE -> EvaluationFactory.pass(labels.isFemalePass())
            Gender.MALE -> EvaluationFactory.fail(labels.isFemaleFail())
            null -> EvaluationFactory.undetermined(labels.isFemaleUndetermined())
        }
    }
}