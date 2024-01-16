package com.hartwig.actin.molecular.datamodel.evidence

data class ExternalTrial(
    val title: String,
    val countries: Set<Country>,
    val url: String,
    val nctId: String
)
