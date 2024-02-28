package com.hartwig.actin.trial.config

data class TrialConfigDatabase(
    val trialDefinitionConfigs: List<TrialDefinitionConfig>,
    val cohortDefinitionConfigs: List<CohortDefinitionConfig>,
    val inclusionCriteriaConfigs: List<InclusionCriteriaConfig>,
    val inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>,
    val unusedRulesToKeep: List<String>
)