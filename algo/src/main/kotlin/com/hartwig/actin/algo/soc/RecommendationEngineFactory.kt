package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.evaluation.RuleMappingResources

class RecommendationEngineFactory(private val ruleMappingResources: RuleMappingResources) {

    fun create(): RecommendationEngine {
        return RecommendationEngine.create(ruleMappingResources)
    }
}