package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

data class ManyIntentsOneInteger(
    val intents: Set<Intent>,
    val integer: Int
)