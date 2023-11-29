package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.CTCDatabaseValidationError
import com.hartwig.actin.trial.CtcDatabaseValidation

data class InterpretedCohortStatus(
    val open: Boolean,
    val slotsAvailable: Boolean
)