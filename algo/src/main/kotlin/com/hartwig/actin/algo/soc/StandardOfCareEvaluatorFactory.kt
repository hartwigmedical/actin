package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources

class StandardOfCareEvaluatorFactory(private val resources: RuleMappingResources) {

    fun create(): StandardOfCareEvaluator {
        return StandardOfCareEvaluator(
            resources.doidModel,
            TreatmentCandidateDatabase(resources.treatmentDatabase),
            EvaluationFunctionFactory.create(resources)
        )
    }
}