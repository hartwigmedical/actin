package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionValidationError

data class CohortStatusInterpretation(
    val status: InterpretedCohortStatus?,
    val cohortDefinitionErrors: List<CohortDefinitionValidationError>,
    val trialDatabaseValidationErrors: List<TrialStatusDatabaseValidationError>
)