package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class WillParticipateInTrialInCountry(private val country: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            country.lowercase().contains("netherlands") ->
                EvaluationFactory.pass("Patient will be participating in $country", "Adequate country of participation")

            else -> EvaluationFactory.fail("Patient will not be participating in $country", "Inadequate country of participation")
        }
    }
}