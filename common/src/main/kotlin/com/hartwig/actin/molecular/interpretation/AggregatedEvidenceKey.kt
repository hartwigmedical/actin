package com.hartwig.actin.molecular.interpretation

data class AggregatedEvidenceKey(
    val codon: Int?,
    val exon: Int?,
    val event: String
)
