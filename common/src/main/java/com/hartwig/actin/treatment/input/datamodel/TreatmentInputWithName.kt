package com.hartwig.actin.treatment.input.datamodel

data class TreatmentInputWithName(
    val treatment: TreatmentCategoryInput,
    val name: String?
)
