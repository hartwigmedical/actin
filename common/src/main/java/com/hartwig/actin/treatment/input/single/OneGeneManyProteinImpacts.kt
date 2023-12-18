package com.hartwig.actin.treatment.input.single

data class OneGeneManyProteinImpacts(
    val geneName: String,
    val proteinImpacts: List<String>
)
