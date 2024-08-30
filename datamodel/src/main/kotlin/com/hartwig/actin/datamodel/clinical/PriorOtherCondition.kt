package com.hartwig.actin.datamodel.clinical

data class PriorOtherCondition(
    val name: String,
    val year: Int? = null,
    val month: Int? = null,
    val doids: Set<String> = emptySet(),
    val category: String,
    val isContraindicationForTherapy: Boolean
)
