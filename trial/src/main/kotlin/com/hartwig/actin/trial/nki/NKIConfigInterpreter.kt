package com.hartwig.actin.trial.nki

import com.hartwig.actin.trial.CtcDatabaseValidation
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.interpretation.ConfigInterpreter

private const val NKI_TRIAL_STATUS_OPEN = "OPEN"

class NKIConfigInterpreter(private val nkiTrialDatabase: NKITrialDatabase) : ConfigInterpreter {
    override fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean {
        return nkiTrialDatabase.findStatus(trialConfig.trialId)?.let {
            return it.studyStatus == NKI_TRIAL_STATUS_OPEN
        } ?: false
    }

    override fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>) {
        TODO("Not yet implemented")
    }

    override fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>) {
        TODO("Not yet implemented")
    }

    override fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
        TODO("Not yet implemented")
    }

    override fun validation(): CtcDatabaseValidation {
        TODO("Not yet implemented")
    }
}