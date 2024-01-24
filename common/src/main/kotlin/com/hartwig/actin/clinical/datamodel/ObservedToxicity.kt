package com.hartwig.actin.clinical.datamodel

data class ObservedToxicity(
    val name: String,
    val categories: Set<String>,
    val grade: Int?
) : Comparable<ObservedToxicity> {
   
    override fun compareTo(other: ObservedToxicity): Int {
        return name.compareTo(other.name)
    }
}
