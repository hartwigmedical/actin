package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration

class HasExhaustedSOCTreatments(private val recommendationEngineFactory: RecommendationEngineFactory) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val recommendationEngine = recommendationEngineFactory.create(EnvironmentConfiguration().algo)
        return when {
            recommendationEngine.standardOfCareCanBeEvaluatedForPatient(record) -> {
                if (recommendationEngine.patientHasExhaustedStandardOfCare(record)) {
                    EvaluationFactory.pass("Patient has exhausted SOC")
                } else {
                    EvaluationFactory.fail("Patient has not exhausted SOC")
                }
            }

            record.oncologicalHistory.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "Patient has not had any prior cancer treatments and therefore undetermined exhaustion of SOC",
                    "Undetermined exhaustion of SOC"
                )
            }

            else -> {
                EvaluationFactory.notEvaluated(
                    "Assumed exhaustion of SOC since patient has had prior cancer treatment",
                    "Assumed exhaustion of SOC"
                )
            }
        }
    }
}