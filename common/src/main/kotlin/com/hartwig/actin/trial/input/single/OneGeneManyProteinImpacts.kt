package com.hartwig.actin.trial.input.single

data class OneGeneManyProteinImpacts(
    val geneName: String,
    val proteinImpacts: Set<String>
)
