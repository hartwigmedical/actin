package com.hartwig.actin.clinical.datamodel.treatment

import java.util.*

enum class OtherTreatmentType(private val category: TreatmentCategory) : TreatmentType {
    ALLOGENIC(TreatmentCategory.TRANSPLANTATION),
    AUTOLOGOUS(TreatmentCategory.TRANSPLANTATION),
    MICROWAVE(TreatmentCategory.ABLATION),
    RADIOFREQUENCY(TreatmentCategory.ABLATION),
    HYPERTHERMIA(TreatmentCategory.ABLATION);

    override fun category(): TreatmentCategory {
        return category
    }

    override fun display(): String {
        return toString().replace("_", " ").lowercase(Locale.getDefault())
    }
}
