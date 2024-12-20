package com.hartwig.actin.datamodel.molecular.evidence

data class CountryDetails(
    val country: Country,
    val hospitalsPerCity: Map<String, Set<Hospital>>
)
