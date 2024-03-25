package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidation

interface ConfigInterpreter {
    fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean?
    fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>)
    fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>)
    fun checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs: List<TrialDefinitionConfig>)

    fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata
    fun validation(): TrialStatusDatabaseValidation
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

    override fun checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs: List<TrialDefinitionConfig>) {
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

    override fun validation(): TrialStatusDatabaseValidation {
        return TrialStatusDatabaseValidation(emptyList(), emptyList())
    }
}

