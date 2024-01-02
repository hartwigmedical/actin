package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable

data class Drug(
    val name: String,
    val drugTypes: Set<DrugType>,
    val category: TreatmentCategory,
    val displayOverride: String? = null,
) : Displayable {

    override fun display(): String {
        return displayOverride ?: name.replace("_", " ").lowercase()
    }
}
