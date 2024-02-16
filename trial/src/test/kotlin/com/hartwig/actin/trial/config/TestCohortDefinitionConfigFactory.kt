package com.hartwig.actin.trial.config

object TestCohortDefinitionConfigFactory {

    val MINIMAL = EmcCohortDefinitionConfig(
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