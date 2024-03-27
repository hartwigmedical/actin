package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources

class RecommendationEngineFactory(private val resources: RuleMappingResources) {

    fun create(): RecommendationEngine {
        return RecommendationEngine(
            resources.doidModel,
            TreatmentCandidateDatabase(resources.treatmentDatabase),
            EvaluationFunctionFactory.create(resources)
        )
    }
}