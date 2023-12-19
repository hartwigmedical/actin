package com.hartwig.actin.trial.input.single

import com.hartwig.actin.trial.input.datamodel.TreatmentInputWithName

data class ManyTreatmentsWithName(
    val treatmentsWithName: List<TreatmentInputWithName>
)
