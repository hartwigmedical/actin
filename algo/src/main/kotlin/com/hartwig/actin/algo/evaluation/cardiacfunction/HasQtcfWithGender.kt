package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Gender

class HasQtcfWithGender(
    private val threshold: Double,
    private val gender: Gender,
    private val evalFunction: (Double) -> EvaluationFunction
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.patient.gender == gender) {
            evalFunction(threshold).evaluate(record)
        } else {
            EvaluationFactory.fail(
                "${gender.display()} QTCF exceptable bound not applicable for ${
                    record.patient.gender.display().lowercase()
                }"
            )
        }
    }
}