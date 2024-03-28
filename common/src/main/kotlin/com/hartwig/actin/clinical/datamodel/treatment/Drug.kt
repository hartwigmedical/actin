package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable

data class Drug(
    val name: String,
    val drugTypes: Set<DrugType>,
    val category: TreatmentCategory,
    val displayOverride: String? = null,
) : Displayable, Comparable<Drug> {

    override fun display(): String {
        return displayOverride ?: name.replace("_", " ").lowercase()
    }

    override fun compareTo(other: Drug): Int {
        return name.compareTo(other.name)
    }

    companion object {
        const val UNKNOWN_PREFIX = "UNKNOWN"
    }
}
