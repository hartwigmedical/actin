package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class WillParticipateInTrialInCountry(private val country: String, private val labels: EvaluationLabels.General) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            country.lowercase().contains("netherlands") -> EvaluationFactory.pass(labels.willParticipateInTrialInCountryPass(country))

            else -> EvaluationFactory.fail(labels.willParticipateInTrialInCountryFail(country))
        }
    }
}