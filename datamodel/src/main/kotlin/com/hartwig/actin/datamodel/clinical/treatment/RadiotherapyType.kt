package com.hartwig.actin.datamodel.clinical.treatment

enum class RadiotherapyType : TreatmentType {
    BRACHYTHERAPY,
    RADIOISOTOPE,
    STEREOTACTIC;

    override val category = TreatmentCategory.RADIOTHERAPY

    override fun display(): String {
        return toString().replace("_", " ").lowercase()
    }
}