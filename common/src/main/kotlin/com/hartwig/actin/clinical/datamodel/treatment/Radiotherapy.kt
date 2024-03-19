package com.hartwig.actin.clinical.datamodel.treatment

data class Radiotherapy(
    override val name: String,
    override val synonyms: Set<String> = emptySet(),
    override val displayOverride: String? = null,
    override val isSystemic: Boolean = false,
    val radioType: RadiotherapyType? = null,
    val isInternal: Boolean? = null
) : Treatment {

    override val treatmentClass = TreatmentClass.RADIOTHERAPY

    override fun types() = setOfNotNull(radioType)
   
    override fun categories() = setOf(TreatmentCategory.RADIOTHERAPY)
}
