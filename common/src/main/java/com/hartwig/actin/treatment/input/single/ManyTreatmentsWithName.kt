package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.treatment.input.datamodel.TreatmentInputWithName

data class ManyTreatmentsWithName(
    val treatmentsWithName: List<TreatmentInputWithName>
)
