package com.hartwig.actin.treatment.input.single

data class OneGeneManyCodons(
    val geneName: String,
    val codons: List<String>
)
