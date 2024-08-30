package com.hartwig.actin.datamodel.clinical.treatment

data class DrugTreatment(
    override val name: String,
    val drugs: Set<Drug>,
    override val synonyms: Set<String> = emptySet(),
    override val displayOverride: String? = null,
    override val isSystemic: Boolean = true,
    val maxCycles: Int? = null
) : Treatment {

    override val treatmentClass = TreatmentClass.DRUG_TREATMENT

    override fun categories() = drugs.map(Drug::category).toSet()

    override fun types() = drugs.flatMap(Drug::drugTypes).toSet()
}
