package com.hartwig.actin.datamodel.clinical.treatment

data class OtherTreatment(
    override val name: String,
    override val isSystemic: Boolean,
    override val synonyms: Set<String> = emptySet(),
    override val displayOverride: String? = null,
    val categories: Set<TreatmentCategory>,
    val types: Set<OtherTreatmentType> = emptySet()
) : Treatment {

    override val treatmentClass = TreatmentClass.OTHER_TREATMENT

    override fun categories() = categories
    override fun types() = types
}
