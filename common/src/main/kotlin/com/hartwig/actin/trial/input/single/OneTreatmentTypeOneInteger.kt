package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

data class OneTreatmentTypeOneInteger(
    val type: TreatmentType,
    val integer: Int
)
