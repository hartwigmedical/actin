package com.hartwig.actin.clinical.datamodel.treatment

data class OtherTreatment(
    override val name: String,
    override val categories: Set<TreatmentCategory>,
    override val isSystemic: Boolean,
    override val synonyms: Set<String> = emptySet(),
    override val displayOverride: String? = null,
    override val types: Set<TreatmentType> = emptySet()
) : Treatment {
    override val treatmentClass = TreatmentClass.OTHER_TREATMENT
}
