package com.hartwig.actin.datamodel.clinical

//TODO(make categories and doid properties redundant)
data class PriorOtherCondition(
    val name: String,
    val year: Int? = null,
    val month: Int? = null,
    val doids: Set<String> = emptySet(),
    val category: String,
    val icdCode: String,
    val isContraindicationForTherapy: Boolean
)
