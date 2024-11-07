package com.hartwig.actin.datamodel.clinical.treatment

data class NoTreatment(
    override val name: String = "No Treatment",
    override val synonyms: Set<String> = emptySet(),
    override val displayOverride: String? = null,
    override val isSystemic: Boolean = false
) : Treatment {

    override val treatmentClass = TreatmentClass.NONE

    override fun categories() = emptySet<TreatmentCategory>()
    override fun types() = emptySet<TreatmentType>()
}
