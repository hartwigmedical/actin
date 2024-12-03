package com.hartwig.actin.datamodel.molecular.evidence

data class ExternalTrial(
    val nctId: String,
    val title: String,
    val molecularMatches: Set<MolecularMatchDetails>,
    val applicableCancerTypes: Set<CancerType>,
    val countries: Set<CountryDetails>,
    val url: String
) : Comparable<ExternalTrial> {

    override fun compareTo(other: ExternalTrial): Int {
        return title.compareTo(other.title)
    }
}