package com.hartwig.actin.trial.input.single

import com.hartwig.actin.trial.input.datamodel.TreatmentCategoryInput

data class OneTreatmentCategoryOrTypeOneInteger(
    val treatment: TreatmentCategoryInput,
    val integer: Int
)