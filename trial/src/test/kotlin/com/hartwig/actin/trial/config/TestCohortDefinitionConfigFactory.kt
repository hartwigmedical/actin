package com.hartwig.actin.trial.config

object TestCohortDefinitionConfigFactory {

    val MINIMAL = CohortDefinitionConfig(
        trialId = "",
        cohortId = "",
        externalCohortIds = emptySet(),
        evaluable = true,
        open = null,
        slotsAvailable = null,
        ignore = false,
        description = ""
    )
}