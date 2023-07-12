package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig

data class TrialConfigDatabase(
    val trialDefinitionConfigs: List<TrialDefinitionConfig>,
    val cohortDefinitionConfigs: List<CohortDefinitionConfig>,
    val inclusionCriteriaConfigs: List<InclusionCriteriaConfig>,
    val inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
)