package com.hartwig.actin.datamodel.clinical.treatment

data class OtherTreatment(
    override val name: String,
    override val isSystemic: Boolean,
    override val synonyms: Set<String> = emptySet(),
    override val treatmentClass: TreatmentClass = TreatmentClass.OTHER_TREATMENT,
    override val displayOverride: String? = null,
    val categories: Set<TreatmentCategory> = emptySet(),
    val types: Set<OtherTreatmentType> = emptySet()
) : Treatment {

    override fun categories() = categories
    override fun types() = types

    companion object {
        val NONE = OtherTreatment(
            name = "None",
            isSystemic = false,
            synonyms = setOf("none", "no treatment"),
            treatmentClass = TreatmentClass.NONE,
        )
    }
}
