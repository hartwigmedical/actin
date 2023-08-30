package com.hartwig.actin.treatment.trial.config

import org.apache.logging.log4j.util.Strings

object TestTrialDefinitionConfigFactory {
    val MINIMAL = TrialDefinitionConfig(
        trialId = Strings.EMPTY,
        open = null,
        acronym = Strings.EMPTY,
        title = Strings.EMPTY
    )
}