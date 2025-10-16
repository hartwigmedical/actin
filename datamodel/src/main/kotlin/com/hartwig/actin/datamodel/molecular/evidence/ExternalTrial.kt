package com.hartwig.actin.datamodel.molecular.evidence

data class ExternalTrial(
    val nctId: String,
    val title: String,
    val acronym: String?,
    val genderMatch: Boolean?,
    val treatments: Set<String>,
    val countries: Set<CountryDetails>,
    val molecularMatches: Set<MolecularMatchDetails>,
    val applicableCancerTypes: Set<CancerType>,
    val url: String
) : Comparable<ExternalTrial> {

    override fun compareTo(other: ExternalTrial): Int {
        return title().compareTo(other.title())
    }

    fun title() : String {
        return acronym ?: title
    }
}