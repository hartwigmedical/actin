package com.hartwig.actin.datamodel.clinical.treatment

enum class OtherTreatmentType(override val category: TreatmentCategory) : TreatmentType {
    ALLOGENIC(TreatmentCategory.TRANSPLANTATION),
    AUTOLOGOUS(TreatmentCategory.TRANSPLANTATION),
    MICROWAVE(TreatmentCategory.ABLATION),
    RADIOFREQUENCY(TreatmentCategory.ABLATION),
    HYPERTHERMIA(TreatmentCategory.ABLATION),
    CYTOREDUCTIVE_SURGERY(TreatmentCategory.SURGERY),
    DEBULKING_SURGERY(TreatmentCategory.SURGERY);

    override fun display(): String {
        return toString().replace("_", " ").lowercase()
    }
}
