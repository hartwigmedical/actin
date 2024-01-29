package com.hartwig.actin.molecular.datamodel.evidence

data class ExternalTrial(
    val title: String,
    val countries: Set<Country>,
    val url: String,
    val nctId: String
) : Comparable<ExternalTrial> {

    override fun compareTo(other: ExternalTrial): Int {
        return title.compareTo(other.title)
    }
}
