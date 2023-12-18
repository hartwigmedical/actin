package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.treatment.input.datamodel.TreatmentCategoryInput

data class OneTreatmentCategoryOrTypeOneInteger(
    val treatment: TreatmentCategoryInput,
    val integer: Int
)