package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Gender

class HasQTCFOfAtMostWithGender(private val maxQTCF: Double, private val gender: Gender) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.patient.gender == gender) {
            ECGMeasureEvaluationFunctions.hasLimitedQTCF(maxQTCF).evaluate(record)
        } else {
            EvaluationFactory.fail(
                "${gender.display()} QTCF exceptable bound not evaluated for ${
                    record.patient.gender.display().lowercase()
                }"
            )
        }
    }
}