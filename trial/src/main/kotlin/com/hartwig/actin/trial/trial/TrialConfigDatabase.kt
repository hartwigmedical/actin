package com.hartwig.actin.trial.trial

import com.hartwig.actin.trial.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.trial.config.TrialDefinitionConfig

data class TrialConfigDatabase(
    val trialDefinitionConfigs: List<TrialDefinitionConfig>,
    val cohortDefinitionConfigs: List<CohortDefinitionConfig>,
    val inclusionCriteriaConfigs: List<InclusionCriteriaConfig>,
    val inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
)