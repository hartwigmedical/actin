package com.hartwig.actin.treatment.trial.config

import org.apache.logging.log4j.util.Strings

object TestCohortDefinitionConfigFactory {
    val MINIMAL = CohortDefinitionConfig(
        trialId = Strings.EMPTY,
        cohortId = Strings.EMPTY,
        ctcCohortIds = setOf(),
        evaluable = true,
        open = null,
        slotsAvailable = null,
        blacklist = false,
        description = Strings.EMPTY
    )
}