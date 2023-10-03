package com.hartwig.actin.trial.trial.config

object TestCohortDefinitionConfigFactory {

    val MINIMAL = CohortDefinitionConfig(
        trialId = "",
        cohortId = "",
        ctcCohortIds = setOf(),
        evaluable = true,
        open = null,
        slotsAvailable = null,
        blacklist = false,
        description = ""
    )
}