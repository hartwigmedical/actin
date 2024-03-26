package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.configuration.AlgoConfiguration

class RecommendationEngineFactory(private val resources: RuleMappingResources) {

    fun create(config: AlgoConfiguration): RecommendationEngine {
        return RecommendationEngine(
            resources.doidModel,
            TreatmentCandidateDatabase(resources.treatmentDatabase),
            EvaluationFunctionFactory.create(resources, config)
        )
    }
}