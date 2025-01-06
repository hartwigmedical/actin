package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Gender

class IsBreastfeeding: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.patient.gender == Gender.MALE) {
            EvaluationFactory.fail("No breastfeeding (is male)")
        } else {
            EvaluationFactory.notEvaluated("Currently assumed that patient is not breastfeeding")
        }
    }
}