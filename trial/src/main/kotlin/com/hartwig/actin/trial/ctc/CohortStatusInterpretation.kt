package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.CTCDatabaseValidationError
import com.hartwig.actin.trial.CohortDefinitionValidationError

data class CohortStatusInterpretation(
    val status: InterpretedCohortStatus?,
    val cohortDefinitionErrors: List<CohortDefinitionValidationError>,
    val ctcDatabaseValidationErrors: List<CTCDatabaseValidationError>
)