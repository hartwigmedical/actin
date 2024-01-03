package com.hartwig.actin.clinical.datamodel.treatment

enum class OtherTreatmentType(override val category: TreatmentCategory) : TreatmentType {
    ALLOGENIC(TreatmentCategory.TRANSPLANTATION),
    AUTOLOGOUS(TreatmentCategory.TRANSPLANTATION),
    MICROWAVE(TreatmentCategory.ABLATION),
    RADIOFREQUENCY(TreatmentCategory.ABLATION),
    HYPERTHERMIA(TreatmentCategory.ABLATION);

    override fun display(): String {
        return toString().replace("_", " ").lowercase()
    }
}
