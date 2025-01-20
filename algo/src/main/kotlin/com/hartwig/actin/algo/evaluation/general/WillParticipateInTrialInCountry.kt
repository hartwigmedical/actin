package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class WillParticipateInTrialInCountry(private val country: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            country.lowercase().contains("netherlands") -> EvaluationFactory.pass("Patient will be participating in $country")

            else -> EvaluationFactory.fail("Patient will not be participating in $country")
        }
    }
}