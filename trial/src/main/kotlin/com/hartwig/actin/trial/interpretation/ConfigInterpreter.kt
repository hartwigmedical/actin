package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.trial.CtcDatabaseValidation
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.datamodel.CohortMetadata

interface ConfigInterpreter {
    fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean?
    fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>)
    fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>)

    fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata
    fun validation(): CtcDatabaseValidation
}

class SimpleConfigInterpreter : ConfigInterpreter {
    override fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean {
        return trialConfig.open ?: false
    }

    override fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>) {
        // no-op
    }

    override fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>) {
        // no-op
    }

    override fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
        return CohortMetadata(
            cohortId = cohortConfig.cohortId,
            evaluable = cohortConfig.evaluable,
            open = cohortConfig.open ?: false,
            slotsAvailable = cohortConfig.slotsAvailable ?: false,
            blacklist = cohortConfig.blacklist,
            description = cohortConfig.description
        )
    }

    override fun validation(): CtcDatabaseValidation {
        return CtcDatabaseValidation(emptyList(), emptyList())
    }
}

